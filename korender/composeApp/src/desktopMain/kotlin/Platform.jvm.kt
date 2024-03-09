package com.zakgof.korender

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.gl.VGL12
import com.zakgof.korender.gl.VGL13
import com.zakgof.korender.gl.VGL14
import com.zakgof.korender.gl.VGL15
import com.zakgof.korender.gl.VGL20
import com.zakgof.korender.gl.VGL30
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.lwjgl.Lwjgl11
import com.zakgof.korender.lwjgl.Lwjgl12
import com.zakgof.korender.lwjgl.Lwjgl13
import com.zakgof.korender.lwjgl.Lwjgl14
import com.zakgof.korender.lwjgl.Lwjgl15
import com.zakgof.korender.lwjgl.Lwjgl20
import com.zakgof.korender.lwjgl.Lwjgl30
import com.zakgof.korender.material.Image
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.swing.SwingUtilities


class JVMPlatform : Platform {

    override val name: String = "Java ${System.getProperty("java.version")}"

    @Composable
    override fun openGL(init: () -> Unit, frame: () -> Unit, resize: (Int, Int) -> Unit) {
        val pixelRatio = detectDevicePixelRatio()
        SwingPanel(modifier = Modifier.fillMaxSize(),
            update = {
                val renderLoop: Runnable = object : Runnable {
                    override fun run() {
                        if (!it.isValid) return
                        it.render()
                        SwingUtilities.invokeLater(this)
                    }
                }
                SwingUtilities.invokeLater(renderLoop)
            },
            factory = {

                VGL11.gl = Lwjgl11()
                VGL12.gl = Lwjgl12()
                VGL13.gl = Lwjgl13()
                VGL14.gl = Lwjgl14()
                VGL15.gl = Lwjgl15()
                VGL20.gl = Lwjgl20()
                VGL30.gl = Lwjgl30()

                val data = GLData()
                data.majorVersion = 3
                data.minorVersion = 0
                // TODO
                data.samples = 1

                val canvas = object : AWTGLCanvas(data) {

                    override fun initGL() {
                        println("OpenGL version: ${effective.majorVersion}.${effective.minorVersion} (Profile: ${effective.profile})")
                        createCapabilities()
                        GL11.glClearColor(0.1f, 0.0f, 0.0f, 0.0f) // TODO move to init
                        init()
                        resize(
                            (this.size.width * pixelRatio[0]).toInt(),
                            (this.size.height * pixelRatio[1]).toInt()
                        )
                    }

                    override fun paintGL() {
                        // TODO: catch exceptions
                        frame()
                        swapBuffers()
                    }
                }
                canvas.addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent?) {
                        resize(
                            (canvas.size.width * pixelRatio[0]).toInt(),
                            (canvas.size.height * pixelRatio[1]).toInt()
                        )
                        println("size ${canvas.size.width}, ${canvas.size.height}")
                    }
                })
                canvas
            }
        )
    }

    private fun detectDevicePixelRatio(): List<Float> {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice;
        val scaleX = device::class.members.firstOrNull { it.name == "getDefaultScaleX" }
            ?.call(device) as Float?
        val scaleY = device::class.members.firstOrNull { it.name == "getDefaultScaleY" }
            ?.call(device) as Float?
        return listOf(scaleX ?: 1.0f, scaleY ?: 1.0f)
    }

    override fun loadImage(stream: InputStream): Image {
        val bufferedImage = ImageIO.read(stream)
        val raster = bufferedImage.raster
        val bytes = when (bufferedImage.type) {
            BufferedImage.TYPE_3BYTE_BGR -> loadBgr((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_4BYTE_ABGR -> loadAbgr((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_BYTE_GRAY -> loadGray((raster.dataBuffer as DataBufferByte).data)
            else -> throw KorenderException("Unknown image format ${bufferedImage.type}")
        }
        val format = when (bufferedImage.type) {
            BufferedImage.TYPE_3BYTE_BGR -> GpuTexture.Format.RGB
            BufferedImage.TYPE_4BYTE_ABGR -> GpuTexture.Format.RGBA
            BufferedImage.TYPE_BYTE_GRAY -> GpuTexture.Format.Gray
            else -> throw KorenderException("Unknown image format ${bufferedImage.type}")
        }
        return JvmImage(
            bufferedImage.width,
            bufferedImage.height,
            bytes,
            format
        )
    }

    private fun loadBgr(data: ByteArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(data.size)
        for (i in 0 until data.size / 3) {
            buffer.put(data[i * 3 + 2])
                .put(data[i * 3 + 1])
                .put(data[i * 3])
        }
        return buffer.flip() as ByteBuffer;
    }

    private fun loadGray(data: ByteArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(data.size)
        buffer.put(data)
        return buffer.flip() as ByteBuffer
    }

    private fun loadAbgr(data: ByteArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(data.size)
        for (i in 0 until data.size / 4) {
            buffer.put(data[i * 4 + 3])
                .put(data[i * 4 + 2])
                .put(data[i * 4 + 1])
                .put(data[i * 4])
        }
        return buffer.flip() as ByteBuffer
    }

}

actual fun getPlatform(): Platform = JVMPlatform()

class JvmImage(
    override val width: Int, override val height: Int,
    override val bytes: ByteBuffer, override val format: GpuTexture.Format
) : Image

