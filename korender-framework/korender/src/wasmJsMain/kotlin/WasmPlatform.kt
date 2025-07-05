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
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.WebGLRenderingContext.Companion.RENDERER
import org.khronos.webgl.WebGLRenderingContext.Companion.SHADING_LANGUAGE_VERSION
import org.khronos.webgl.WebGLRenderingContext.Companion.VENDOR
import org.khronos.webgl.WebGLRenderingContext.Companion.VERSION
import org.khronos.webgl.toInt8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
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
//                ctx.fillStyle = listOf("red".toJsString(), "blue".toJsString(), "magenta".toJsString())[it % 3]
//                ctx.fillRect((it % 16) * 256.0, (it / 16) * 256.0, 256.0, 256.0)
//                ctx.fillStyle = "white".toJsString()
                ctx.fillText(
                    texts[it],
                    (it % 16) * 256.0,
                    (it / 16) * 256.0 + 256.0 - maxDescent
                )
            }
            val imageData = ctx.getImageData(
                0.0,
                0.0,
                ctx.canvas.width.toDouble(),
                ctx.canvas.height.toDouble()
            )
            val uint8ClampedArray: Uint8ClampedArray = imageData.data
            val uint8Array = Uint8Array(uint8ClampedArray.buffer, uint8ClampedArray.byteOffset, uint8ClampedArray.length)
            val image = InternalImage(
                ctx.canvas.width,
                ctx.canvas.height,
                NativeByteBuffer(uint8Array),
                Image.Format.RGBA
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
        image.onerror = { a, b, c, d, e ->
            result.completeExceptionally(KorenderException("$a $b $c $d $e"))
            null
        }
        image.onload = {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            canvas.width = image.width
            canvas.height = image.height
            context.drawImage(image, 0.0, 0.0)
            val imageData = context.getImageData(
                0.0,
                0.0,
                canvas.width.toDouble(),
                canvas.height.toDouble()
            )
            val uint8ClampedArray: Uint8ClampedArray = imageData.data
            val uint8Array = Uint8Array(uint8ClampedArray.buffer, uint8ClampedArray.byteOffset, uint8ClampedArray.length)
            result.complete(InternalImage(imageData.width, imageData.height, NativeByteBuffer(uint8Array), Image.Format.RGBA))
        }
        return result
    }

    actual fun createImage(width: Int, height: Int, format: Image.Format): InternalImage {
        val pixelBytes = when(format) {
            Image.Format.RGB -> 3
            Image.Format.RGBA -> 4
            Image.Format.Gray -> 1
            Image.Format.Gray16 -> 2
        }
        val buffer = NativeByteBuffer(width * height * pixelBytes)
        return InternalImage(width, height, buffer, format)
    }

    actual fun nanoTime(): Long =
        (performanceNow() * 1_000_000).toLong()

}

@OptIn(DelicateCoroutinesApi::class)
@Composable
actual fun Korender(
    appResourceLoader: ResourceLoader,
    block: KorenderContext.() -> Unit
) {
    var engine: Engine? by remember { mutableStateOf(null) }
    val canvas by remember { mutableStateOf(document.createElement("canvas") as HTMLCanvasElement) }

    Column(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val crds = coordinates.boundsInWindow()

            canvas.width = (crds.width / window.devicePixelRatio).toInt()
            canvas.height = (crds.height / window.devicePixelRatio).toInt()
            canvas.style.apply {
                position = "absolute"
                left = "${(crds.left / window.devicePixelRatio).toInt()}px"
                top = "${(crds.top / window.devicePixelRatio)}px"
                background = "black"
            }
            engine?.resize(
                (crds.width / window.devicePixelRatio).toInt(),
                (crds.height / window.devicePixelRatio).toInt()
            )
        }.fillMaxSize() // TODO
    )
    {
    }

    DisposableEffect(Unit) {
        val body = document.body!!
        canvas.width = 0
        canvas.height = 0
        canvas.style.apply {
            position = "absolute"
            left = "0px"
            top = "0px"
            background = "black"
        }
        body.appendChild(canvas)

        val gl2 = canvas.getContext("webgl2")
        if (gl2 == null) {
            println("WebGL2 is not supported in this browser")
        }

        val gl = gl2 as WebGL2RenderingContext

        println("Renderer: " + gl.getParameter(RENDERER));
        println("Vendor: " + gl.getParameter(VENDOR));
        println("Version: " + gl.getParameter(VERSION));
        println("GLSL version: " + gl.getParameter(SHADING_LANGUAGE_VERSION));
        println("Supported extensions")

        val exts = gl.getSupportedExtensions()!!
        (0 until exts.length).map { exts[it] }.forEach {
            println(" - " + it.toString())
        }

        GL.gl = gl

        val async = object : AsyncContext {
            override val appResourceLoader = appResourceLoader
            override fun <R> call(function: suspend () -> R): Deferred<R> =
                GlobalScope.async { function() }
        }

        engine = Engine(800, 600, async, block)

        animate(window, canvas, engine!!)

        fun Short.toButton() : TouchEvent.Button = when (this) {
            0.toShort() -> TouchEvent.Button.LEFT
            2.toShort() -> TouchEvent.Button.RIGHT
            else -> TouchEvent.Button.NONE
        }

        canvas.addEventListener("webglcontextlost") {
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
            val te = event as org.w3c.dom.TouchEvent
            te.touches[0]?.let { touch ->
                val x = touch.pageX - canvas.offsetLeft
                val y = touch.pageY - canvas.offsetTop
                GlobalScope.launch {
                    engine?.pushTouch(TouchEvent(type, TouchEvent.Button.LEFT, x.toFloat(), y.toFloat()))
                }
            }
            // TODO improve this POC
            if (type == TouchEvent.Type.UP && te.touches[0] == null) {
                te.changedTouches[0]?.let { touch ->
                    val x = touch.pageX - canvas.offsetLeft
                    val y = touch.pageY - canvas.offsetTop
                    GlobalScope.launch {
                        engine?.pushTouch(TouchEvent(type, TouchEvent.Button.LEFT, x.toFloat(), y.toFloat()))
                    }
                }
            }
        }

        fun sendKey(type: KeyEvent.Type, event: Event) {
            val ke = event as org.w3c.dom.events.KeyboardEvent
            GlobalScope.launch {
                engine?.pushKey(KeyEvent(type, ke.key))
            }
        }

        canvas.addEventListener("mouseup") {
            sendMouseTouch(TouchEvent.Type.UP, it)
        }
        canvas.addEventListener("mousedown") {
            sendMouseTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.addEventListener("mousemove") {
            sendMouseTouch(TouchEvent.Type.MOVE, it)
        }

        canvas.addEventListener("touchstart") {
            sendTouchTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.addEventListener("touchend") {
            sendTouchTouch(TouchEvent.Type.UP, it)
        }
        canvas.addEventListener("touchmove") {
            sendTouchTouch(TouchEvent.Type.MOVE, it)
        }
        // TODO cleanup
        document.addEventListener("keydown") {
            sendKey(KeyEvent.Type.DOWN, it)
        }
        document.addEventListener("keyup") {
            sendKey(KeyEvent.Type.UP, it)
        }

        onDispose {
            println("Destroying canvas")
            canvas.remove()
        }
    }

}

private fun animate(window: Window, canvas: HTMLCanvasElement, engine: Engine) {
    window.requestAnimationFrame {
        try {
            if (canvas.isConnected) {
                engine.frame()
                animate(window, canvas, engine)
            } else {
                println("Canvas not connected")
            }
        } catch (e: Throwable) {
            println(e)
            e.printStackTrace()
        }
    }
}

