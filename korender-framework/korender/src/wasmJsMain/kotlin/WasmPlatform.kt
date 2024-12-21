package com.zakgof.korender

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.gl.GL
import com.zakgof.korender.image.Image
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Color
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Promise

actual fun getPlatform(): Platform = WasmPlatform()

internal fun Byte.toClampedFloat(): Float = this.toInt().and(0xFF).toFloat()

internal class WasmImage(
    override val width: Int,
    override val height: Int,
    private val byteArray: ByteArray,
    override val format: GpuTexture.Format
) : Image {
    override val bytes = Byter(byteArray)
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

fun jsLoadFont(fontArray: Int8Array): FontFace = js(
    """
        {
            const fontBlob = new Blob([fontArray], { type: 'font/ttf' })
            const fontUrl = URL.createObjectURL(fontBlob)
            return new FontFace('KorenderFont', 'url(' + fontUrl + ')')
        }
    """
)

fun jsAddFont(fontFace: FontFace): JsAny =
    js(
        """
      {
          console.log("d.f", document.fonts)
          document.fonts.add(fontFace)
          console.log("added", document.fonts)
          return 0
      }
    """
    )

external class FontFace : JsAny {
    fun load(): Promise<FontFace>
}


internal class WasmPlatform : Platform {

    override val name: String = "Wasm"

    @OptIn(DelicateCoroutinesApi::class)
    override fun loadFont(bytes: ByteArray): Deferred<FontDef> {
        val ffLoader = jsLoadFont(bytes.toInt8Array())
        println("loadFont BP 1")
        return GlobalScope.async {
            println("Async BP 2")
            val fontFace = ffLoader.load().await<FontFace>()
            println("Async BP 3 $fontFace")
            jsAddFont(fontFace)
            println("Async BP 4")
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.height = 16 * 256
            canvas.width = 16 * 256
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            ctx.font = "256px KorenderFont"
            ctx.fillStyle = "white".toJsString()
            ctx.fillStyle = "white".toJsString()
            ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
            val texts = (0 until 128).map { "" + it.toChar() }
            val metrics = texts.map { ctx.measureText(it) }
            val widths = metrics.map { it.width.toFloat() / 256.0f }.toFloatArray()
            val maxDescent = metrics.maxOfOrNull { it.fontBoundingBoxDescent }!!
            texts.indices.forEach {
                ctx.fillText(
                    texts[it],
                    (it % 16) * 256.0,
                    (it / 16) * 256.0 + 256.0 - maxDescent - 1.0
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
            println("Font image byte array length is ${byteArray.size}")
            val image = WasmImage(
                ctx.canvas.width,
                ctx.canvas.height,
                byteArray,
                GpuTexture.Format.RGBA
            )
            FontDef(image, widths)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun loadImage(bytes: ByteArray, type: String): Deferred<Image> {
        println("Wasm start loading image from ${bytes.size} bytes")
        val result = CompletableDeferred<Image>()

        val base64Data = Base64.encode(bytes)
        val image = document.createElement("img") as HTMLImageElement
        image.src = "data:image/$type;base64,$base64Data"
        println("Wasm loading image: ${image.src}")
        image.onerror = { a, b, c, d, e ->
            result.completeExceptionally(KorenderException("$a $b $c $d $e"))
            null
        }
        image.onload = {
            println("Wasm image loaded")
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            canvas.width = image.width
            canvas.height = image.height
            println("Wasm canvas $canvas")
            context.drawImage(image, 0.0, 0.0)
            val imageData = context.getImageData(
                0.0,
                0.0,
                canvas.width.toDouble(),
                canvas.height.toDouble()
            )
            val uint8ClampedArray: Uint8ClampedArray = imageData.data
            val byteArray = ByteArray(uint8ClampedArray.length) { uint8ClampedArray[it] }
            println("Image byte array length is ${byteArray.size}")
            result.complete(
                WasmImage(
                    imageData.width,
                    imageData.height,
                    byteArray,
                    GpuTexture.Format.RGBA
                )
            )
        }
        return result
    }

    @Composable
    override fun OpenGL(
        init: (Int, Int) -> Unit,
        frame: () -> Unit,
        resize: (Int, Int) -> Unit,
        touch: (touchEvent: TouchEvent) -> Unit
    ) {

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
                resize(
                    (crds.width / window.devicePixelRatio).toInt(),
                    (crds.height / window.devicePixelRatio).toInt()
                )
            }.fillMaxSize().background(color = androidx.compose.ui.graphics.Color.Magenta)
        )
        {
        }

        DisposableEffect(Unit) {
            val body = document.body!!
            canvas.width = 800
            canvas.height = 600
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
            init(800, 600)

            animate(window, canvas, frame)


            canvas.addEventListener("webglcontextlost") {
                it.preventDefault();  // Prevent the default behavior of losing the context.
                println("WebGL context lost !")
            }

            onDispose {
                println("Destroying canvas")
                canvas.remove()
            }
        }
    }

    private fun animate(window: Window, canvas: HTMLCanvasElement, frame: () -> Unit) {
        window.requestAnimationFrame {
            try {
                if (canvas.isConnected) {
                    println("------------")
                    frame()
                    animate(window, canvas, frame)
                } else {
                    println("Canvas not connected")
                }
            } catch (e: Throwable) {
                println(e)
                e.printStackTrace()
            }
        }
    }

    override fun nanoTime(): Long =
        (performanceNow() * 1_000_000).toLong()

}

fun performanceNow(): Double = js("performance.now()")

