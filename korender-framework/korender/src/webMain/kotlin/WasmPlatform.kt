package com.zakgof.korender

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.gl.GL
import com.zakgof.korender.impl.image.InternalImage
import js.typedarrays.Uint8Array
import js.typedarrays.toInt8Array
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import web.canvas.CanvasRenderingContext2D
import web.canvas.ID
import web.device.devicePixelRatio
import web.dom.document
import web.dom.errorEvent
import web.dom.keyDownEvent
import web.dom.keyUpEvent
import web.dom.loadEvent
import web.dom.mouseDownEvent
import web.dom.mouseMoveEvent
import web.dom.mouseUpEvent
import web.dom.touchEndEvent
import web.dom.touchMoveEvent
import web.dom.touchStartEvent
import web.events.Event
import web.events.addHandler
import web.gl.ID
import web.gl.WebGL2RenderingContext
import web.gl.WebGL2RenderingContext.Companion.RENDERER
import web.gl.WebGL2RenderingContext.Companion.SHADING_LANGUAGE_VERSION
import web.gl.WebGL2RenderingContext.Companion.VERSION
import web.gl.WebGLRenderingContext.Companion.VENDOR
import web.html.HTMLCanvasElement
import web.html.HTMLImageElement
import web.html.webglContextLostEvent
import web.keyboard.KeyboardEvent
import web.mouse.MouseButton
import web.mouse.MouseButtons
import web.mouse.MouseEvent
import web.mouse.PRIMARY
import web.mouse.SECONDARY
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalWasmJsInterop::class)
internal actual object Platform {

    actual val target = KorenderContext.TargetPlatform.Web

    @OptIn(DelicateCoroutinesApi::class)
    internal actual fun loadFont(bytes: ByteArray): Deferred<FontDef> {
        val ffLoader = jsLoadFont(bytes.toInt8Array())
        return GlobalScope.async {
            val fontFace = ffLoader.load().await<FontFace>()
            jsAddFont(fontFace)
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.height = 16 * 256
            canvas.width = 16 * 256
            val ctx = canvas.getContext(CanvasRenderingContext2D.ID)!!
            ctx.font = "256px KorenderFont"
            val texts = (0 until 128).map { "" + it.toChar() }
            val origMetrics = texts.map { ctx.measureText(it) }
            val actualBbHeight = origMetrics.maxOfOrNull { it.actualBoundingBoxDescent + it.actualBoundingBoxAscent }!!
            val fittingHeight = (256 * 256 / actualBbHeight).toInt() + 1
            println("Effective height: $fittingHeight")
            ctx.font = "${fittingHeight}px KorenderFont"
            ctx.fillStyle = "white".toJsString()

            val metrics = texts.map { ctx.measureText(it) }
            val maxDescent = origMetrics.maxOfOrNull { it.actualBoundingBoxDescent }!!

            ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
            val widths = metrics.map { it.width.toFloat() / 256.0f }.toFloatArray()

            texts.indices.forEach {
                ctx.fillText(
                    texts[it],
                    (it % 16) * 256.0,
                    (it / 16) * 256.0 + 256.0 - maxDescent
                )
            }
            val imageData = ctx.getImageData(
                0,
                0,
                ctx.canvas.width,
                ctx.canvas.height
            )
            val uint8ClampedArray = imageData.data
            val uint8Array = Uint8Array(uint8ClampedArray.buffer, uint8ClampedArray.byteOffset, uint8ClampedArray.length)
            val image = InternalImage(
                ctx.canvas.width,
                ctx.canvas.height,
                NativeByteBuffer(uint8Array),
                PixelFormat.RGBA
            )
            FontDef(image, widths)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    internal actual fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage> {
        val result = CompletableDeferred<InternalImage>()
        val base64Data = Base64.encode(bytes)
        val image = document.createElement("img") as HTMLImageElement
        image.src = "data:image/$type;base64,$base64Data"

        image.errorEvent.addHandler {
            result.completeExceptionally(KorenderException("Image loading error"))
        }

        image.loadEvent.addHandler {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val context = canvas.getContext(CanvasRenderingContext2D.ID)!!
            canvas.width = image.width
            canvas.height = image.height
            context.drawImage(image, 0.0, 0.0)
            val imageData = context.getImageData(
                0,
                0,
                canvas.width,
                canvas.height
            )
            val uint8ClampedArray = imageData.data
            val uint8Array = Uint8Array(uint8ClampedArray.buffer, uint8ClampedArray.byteOffset, uint8ClampedArray.length)
            result.complete(InternalImage(imageData.width, imageData.height, NativeByteBuffer(uint8Array), PixelFormat.RGBA))
        }
        return result
    }

    actual fun createImage(width: Int, height: Int, format: PixelFormat): InternalImage {
        val pixelBytes = when (format) {
            PixelFormat.RGB -> 3
            PixelFormat.RGBA -> 4
            PixelFormat.Gray -> 1
            PixelFormat.Gray16 -> 2
        }
        val buffer = NativeByteBuffer(width * height * pixelBytes)
        return InternalImage(width, height, buffer, format)
    }

    actual fun nanoTime(): Long =
        (performanceNow() * 1_000_000).toLong()

}

@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
@Composable
actual fun Korender(
    appResourceLoader: ResourceLoader,
    block: KorenderContext.() -> Unit,
) {
    var engine: Engine? by remember { mutableStateOf(null) }
    val canvas by remember { mutableStateOf(document.createElement("canvas") as HTMLCanvasElement) }

    Column(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val crds = coordinates.boundsInWindow()

            canvas.width = (crds.width / devicePixelRatio).toInt()
            canvas.height = (crds.height / devicePixelRatio).toInt()
            canvas.style.apply {
                position = "absolute"
                left = "${(crds.left / devicePixelRatio).toInt()}px"
                top = "${(crds.top / devicePixelRatio)}px"
                background = "black"
            }
            engine?.resize(
                (crds.width / devicePixelRatio).toInt(),
                (crds.height / devicePixelRatio).toInt()
            )
        }.fillMaxSize() // TODO
    )
    {
    }

    DisposableEffect(Unit) {
        canvas.width = 0
        canvas.height = 0
        canvas.style.apply {
            position = "absolute"
            left = "0px"
            top = "0px"
            // background = "black"
        }
        document.appendChild(canvas)

        val gl2 = canvas.getContext(WebGL2RenderingContext.ID)
        if (gl2 == null) {
            println("WebGL2 is not supported in this browser")
        }

        val gl = gl2!!

        println("Renderer: " + gl.getParameter(RENDERER));
        println("Vendor: " + gl.getParameter(VENDOR));
        println("Version: " + gl.getParameter(VERSION));
        println("GLSL version: " + gl.getParameter(SHADING_LANGUAGE_VERSION));
        println("Supported extensions")

        val exts = gl.getSupportedExtensions()!!
        (0 until exts.length).map { exts[it] }.forEach {
            println(" - $it")
        }

        GL.gl = gl

        fun animate() {
            window.requestAnimationFrame {
                try {
                    if (canvas.isConnected) {
                        GL.gl = gl
                        engine!!.frame()
                        animate()
                    } else {
                        println("Canvas disconnected")
                    }
                } catch (e: Throwable) {
                    println(e)
                    e.printStackTrace()
                }
            }
        }

        engine = Engine(800, 600, appResourceLoader, block)

        animate()

        fun MouseButton.toButton() = when (this) {
            MouseButtons.PRIMARY -> TouchEvent.Button.LEFT
            MouseButtons.SECONDARY -> TouchEvent.Button.RIGHT
        }

        canvas.webglContextLostEvent.addHandler {
            it.preventDefault()
            println("WebGL context lost !")
        }

        fun sendMouseTouch(type: TouchEvent.Type, event: Event) {
            val me = event as MouseEvent
            val x = me.pageX - canvas.offsetLeft
            val y = me.pageY - canvas.offsetTop
            GlobalScope.launch {
                engine?.pushTouch(TouchEvent(type, me.button.toButton(), x.toFloat(), y.toFloat()))
            }
        }

        fun sendTouchTouch(type: TouchEvent.Type, event: Event) {
            val te = event as web.touch.TouchEvent
            te.touches[0].let { touch ->
                val x = touch.pageX - canvas.offsetLeft
                val y = touch.pageY - canvas.offsetTop
                GlobalScope.launch {
                    engine?.pushTouch(TouchEvent(type, TouchEvent.Button.LEFT, x.toFloat(), y.toFloat()))
                }
            }
            // TODO improve this POC
            if (type == TouchEvent.Type.UP) {
                te.changedTouches[0].let { touch ->
                    val x = touch.pageX - canvas.offsetLeft
                    val y = touch.pageY - canvas.offsetTop
                    GlobalScope.launch {
                        engine?.pushTouch(TouchEvent(type, TouchEvent.Button.LEFT, x.toFloat(), y.toFloat()))
                    }
                }
            }
        }

        fun sendKey(type: KeyEvent.Type, event: Event) {
            val ke = event as KeyboardEvent
            GlobalScope.launch {
                engine?.pushKey(KeyEvent(type, ke.key))
            }
        }

        canvas.mouseUpEvent.addHandler {
            sendMouseTouch(TouchEvent.Type.UP, it)
        }
        canvas.mouseDownEvent.addHandler {
            sendMouseTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.mouseMoveEvent.addHandler {
            sendMouseTouch(TouchEvent.Type.MOVE, it)
        }

        canvas.touchStartEvent.addHandler {
            sendTouchTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.touchEndEvent.addHandler  {
            sendTouchTouch(TouchEvent.Type.UP, it)
        }
        canvas.touchMoveEvent.addHandler {
            sendTouchTouch(TouchEvent.Type.MOVE, it)
        }
        // TODO cleanup
        document.keyDownEvent.addHandler {
            sendKey(KeyEvent.Type.DOWN, it)
        }
        document.keyUpEvent.addHandler {
            sendKey(KeyEvent.Type.UP, it)
        }

        onDispose {
            println("Destroying canvas")
            canvas.remove()
        }
    }

}

