package com.zakgof.korender

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.zakgof.korender.gl.GL
import com.zakgof.korender.image.Image
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.input.TouchEvent
import kotlinx.browser.document
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement

actual fun getPlatform(): Platform = WasmPlatform()

internal class WasmPlatform : Platform {

    override val name: String = "Wasm"

    override fun loadImage(bytes: ByteArray): Image {
        TODO("Not yet implemented")
    }

    override fun loadFont(bytes: ByteArray): FontDef {
        TODO("Not yet implemented")
    }

    @Composable
    override fun OpenGL(
        init: (Int, Int) -> Unit,
        frame: () -> Unit,
        resize: (Int, Int) -> Unit,
        touch: (touchEvent: TouchEvent) -> Unit
    ) {
        DisposableEffect(Unit) {
            val parent = document.getElementsByTagName("canvas").item(0)
            val canvas = parent as HTMLCanvasElement
            val gl = canvas.getContext("webgl") as WebGLRenderingContext?
            if (gl == null) {
                println("WebGL is not supported in this browser")
            }

            val canvas2 = document.createElement("canvas") as HTMLCanvasElement
            canvas2.width = 800
            canvas2.height = 600
            document.body!!.appendChild(canvas2)

            // Get WebGL context
            val gl2 = canvas2.getContext("webgl") as WebGLRenderingContext?
            if (gl2 == null) {
                println("WebGL 2 is not supported in this browser")
            }

            println(gl2)

            GL.gl = gl2
            init(800, 600)
            onDispose {
            }
        }
    }


    override fun nanoTime(): Long {
        return 1L
    }
}