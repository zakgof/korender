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
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.image.Image
import com.zakgof.korender.math.Color
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.WebGLRenderingContext.Companion.RENDERER
import org.khronos.webgl.WebGLRenderingContext.Companion.SHADING_LANGUAGE_VERSION
import org.khronos.webgl.WebGLRenderingContext.Companion.VENDOR
import org.khronos.webgl.WebGLRenderingContext.Companion.VERSION
import org.khronos.webgl.get
import org.khronos.webgl.toInt8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal fun Byte.toClampedFloat(): Float = this.toInt().and(0xFF).toFloat()

internal class WasmImage(
    override val width: Int,
    override val height: Int,
    private val byteArray: ByteArray,
    override val format: GlGpuTexture.Format = GlGpuTexture.Format.RGBA
) : Image {
    override val bytes = NativeByteBuffer(byteArray)
    override fun pixel(x: Int, y: Int): Color {
        val base = (x + y * width) * 4
        return Color(
            byteArray[base + 3].toClampedFloat(),
            byteArray[base].toClampedFloat(),
            byteArray[base + 1].toClampedFloat(),
            byteArray[base + 2].toClampedFloat()
        )
    }
}

internal actual object Platform {

    actual val name: String = "Wasm"

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
            val byteArray = ByteArray(uint8ClampedArray.length) { uint8ClampedArray[it] }
            val image = WasmImage(
                ctx.canvas.width,
                ctx.canvas.height,
                byteArray
            )
//            println("FONT IMAGE DUMP")
//            println(canvas.toDataURL())
            FontDef(image, widths)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    internal actual fun loadImage(bytes: ByteArray, type: String): Deferred<Image> {
        val result = CompletableDeferred<Image>()
        val base64Data = Base64.encode(bytes)
        val image = document.createElement("img") as HTMLImageElement
        image.src = "data:image/$type;base64,$base64Data"
        image.onerror = { a, b, c, d, e ->
            result.completeExceptionally(KorenderException("$a $b $c $d $e"))
            null
        }
        println(image.src)
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
            val byteArray = ByteArray(uint8ClampedArray.length) { uint8ClampedArray[it] }
            result.complete(WasmImage(imageData.width, imageData.height, byteArray))
        }
        return result
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

        canvas.addEventListener("webglcontextlost") {
            it.preventDefault()
            println("WebGL context lost !")
        }

        fun sendTouch(type: TouchEvent.Type, event: Event) {
            val me = event as MouseEvent
            val x = me.pageX - canvas.offsetLeft
            val y = me.pageY - canvas.offsetTop
            GlobalScope.launch {
                engine?.pushTouch(TouchEvent(type, x.toFloat(), y.toFloat()))
            }
        }

        canvas.addEventListener("mouseup") {
            sendTouch(TouchEvent.Type.UP, it)
        }
        canvas.addEventListener("mousedown") {
            sendTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.addEventListener("mousemove") {
            sendTouch(TouchEvent.Type.MOVE, it)
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
                println("------------")
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

