package com.zakgof.korender

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.zakgof.korender.gl.GL
import com.zakgof.korender.image.Image
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.preReadResources
import com.zakgof.korender.input.TouchEvent
import kotlinx.browser.document
import kotlinx.browser.window
import org.khronos.webgl.WebGLRenderingContext.Companion.RENDERER
import org.khronos.webgl.WebGLRenderingContext.Companion.SHADING_LANGUAGE_VERSION
import org.khronos.webgl.WebGLRenderingContext.Companion.VENDOR
import org.khronos.webgl.WebGLRenderingContext.Companion.VERSION
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Window

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

            val body = document.body!!
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = 800
            canvas.height = 600
            canvas.style.apply {
                position = "absolute"
                left = "100px"
                top = "100px"
                background = "darkblue"
                border = "1px solid blue"
            }
            body.appendChild(canvas)

            val gl2 = canvas.getContext("webgl2")
            if (gl2 == null) {
                println("WebGL2 is not supported in this browser")
            } else {
                println("WebGL2 is just fain! $gl2")
            }

            println(gl2!!::class)

            val gl = gl2 as WebGL2RenderingContext

            println("Renderer: " +  gl.getParameter(RENDERER));
            println("Vendor: " +  gl.getParameter(VENDOR));
            println("Version: " +  gl.getParameter(VERSION));
            println("GLSL version: " +  gl.getParameter(SHADING_LANGUAGE_VERSION));

            GL.gl = gl
            init(800, 600)

            preReadResources(
                "shader/standart.vert",
                "shader/standart.frag",
                "shader/lib/header.glsl",
                "shader/lib/texturing.glsl",
                "shader/lib/light.glsl",
                "shader/lib/sky.glsl",
                "shader/lib/noise.glsl",
                "shader/lib/blur.glsl",
                "shader/sky/sky.vert",
                "shader/sky/sky.frag",
                "shader/sky/fastcloud.plugin.frag",
                "shader/billboard.vert",
                "shader/screen.vert",
                "shader/effect/adjust.frag"
            ) {
                animate(window, frame)
            }

            onDispose {
            }
        }
    }

    private fun animate(window: Window, frame: () -> Unit) {
        window.requestAnimationFrame {
            println("starting frame $it")
            try {
                frame()
                println("ending frame")
                animate(window, frame)
            } catch (e: Exception) {
                println(e)
                e.printStackTrace()
            }
        }
    }


    override fun nanoTime(): Long {
        return 1L
    }
}