package com.zakgof.korender

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gl.VGL12
import com.zakgof.korender.impl.gl.VGL13
import com.zakgof.korender.impl.gl.VGL14
import com.zakgof.korender.impl.gl.VGL15
import com.zakgof.korender.impl.gl.VGL20
import com.zakgof.korender.impl.gl.VGL30
import com.zakgof.korender.impl.glgpu.BufferUtils
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.lwjgl.Lwjgl11
import com.zakgof.korender.lwjgl.Lwjgl12
import com.zakgof.korender.lwjgl.Lwjgl13
import com.zakgof.korender.lwjgl.Lwjgl14
import com.zakgof.korender.lwjgl.Lwjgl15
import com.zakgof.korender.lwjgl.Lwjgl20
import com.zakgof.korender.lwjgl.Lwjgl30
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferUShort
import java.awt.image.Raster
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.math.max
import kotlin.math.round


class JVMPlatform : Platform {

    override val name: String = "Java ${System.getProperty("java.version")}"

    @Composable
    override fun openGL(
        init: (Int, Int) -> Unit,
        frame: () -> Unit,
        resize: (Int, Int) -> Unit,
        touch: (touchEvent: TouchEvent) -> Unit
    ) {
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
                data.swapInterval = 0
                data.majorVersion = 3
                data.minorVersion = 0
                // TODO
                data.samples = 1

                val canvas = object : AWTGLCanvas(data) {

                    override fun initGL() {
                        println("OpenGL version: ${effective.majorVersion}.${effective.minorVersion} (Profile: ${effective.profile})")
                        createCapabilities()
                        init(
                            (this.size.width * pixelRatio[0]).toInt(),
                            (this.size.height * pixelRatio[1]).toInt()
                        )
                    }

                    override fun paintGL() {
                        frame()
                        swapBuffers()
                    }
                }
                canvas.addMouseMotionListener(object : MouseMotionAdapter() {
                    override fun mouseMoved(e: MouseEvent) =
                        sendTouch(touch, canvas, pixelRatio, TouchEvent.Type.MOVE, e.x, e.y)

                    override fun mouseDragged(e: MouseEvent) =
                        sendTouch(touch, canvas, pixelRatio, TouchEvent.Type.MOVE, e.x, e.y)
                })
                canvas.addMouseListener(object : MouseAdapter() {
                    override fun mousePressed(e: MouseEvent) =
                        sendTouch(touch, canvas, pixelRatio, TouchEvent.Type.DOWN, e.x, e.y)

                    override fun mouseReleased(e: MouseEvent) =
                        sendTouch(touch, canvas, pixelRatio, TouchEvent.Type.UP, e.x, e.y)
                })

                canvas.addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent?) {
                        canvas.runInContext {
                            resize(
                                (canvas.size.width * pixelRatio[0]).toInt(),
                                (canvas.size.height * pixelRatio[1]).toInt()
                            )
                        }
                    }
                })
                canvas
            }
        )
    }

    private fun sendTouch(
        touch: (touchEvent: TouchEvent) -> Unit,
        canvas: AWTGLCanvas,
        pixelRatio: List<Float>,
        type: TouchEvent.Type,
        ex: Int,
        ey: Int
    ) = canvas.runInContext {
        touch(TouchEvent(type, ex * pixelRatio[0], ey * pixelRatio[1]))
    }

    private fun detectDevicePixelRatio(): List<Float> {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val scaleX = device::class.members.firstOrNull { it.name == "getDefaultScaleX" }
            ?.call(device) as Float?
        val scaleY = device::class.members.firstOrNull { it.name == "getDefaultScaleY" }
            ?.call(device) as Float?
        return listOf(scaleX ?: 1.0f, scaleY ?: 1.0f)
    }

    override fun loadImage(stream: InputStream): Image = image(ImageIO.read(stream))

    private fun loadBgr(data: ByteArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(data.size)
        for (i in 0 until data.size / 3) {
            buffer.put(data[i * 3 + 2])
                .put(data[i * 3 + 1])
                .put(data[i * 3])
        }
        return buffer.flip() as ByteBuffer
    }

    private fun loadGray(data: ByteArray): ByteBuffer {
        val buffer = BufferUtils.createByteBuffer(data.size)
        buffer.put(data)
        return buffer.flip() as ByteBuffer
    }

    // TODO: test
    private fun loadGray16(data: ShortArray): ByteBuffer {
        val bb = BufferUtils.createByteBuffer(data.size * 2)
        val buffer = bb.asShortBuffer()
        buffer.put(data)
        return bb.flip()
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

    override fun loadFont(stream: InputStream): FontDef {
        val cell = 256
        val originalFont = Font.createFont(Font.TRUETYPE_FONT, stream)
        val img = BufferedImage(cell * 16, cell * 16, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = img.graphics
        graphics.font = originalFont
        val fmOriginal = graphics.fontMetrics

        val maxwidthheight =
            (0 until 128).map { fmOriginal.getStringBounds("" + it.toChar(), graphics) }
                .maxOf { max(it.width, it.height) }
        val fontSize = cell / maxwidthheight

        val font = originalFont.deriveFont(round(fontSize.toFloat()))
        graphics.font = font
        graphics.color = Color.WHITE
        val widths = FloatArray(128)
        val fm = graphics.fontMetrics
        for (c in 0 until 128) {
            widths[c] = fm.charWidth(c).toFloat() / cell
            if (font.canDisplay(c)) {
                graphics.drawString(
                    "" + c.toChar(),
                    (c % 16) * cell,
                    (c / 16) * cell + cell - fm.maxDescent - 1
                )
            }
        }
        graphics.dispose()
        val image = image(img)
        return FontDef(image, widths)
    }

    private fun image(bufferedImage: BufferedImage): Image {
        val raster = bufferedImage.raster
        val bytes = when (bufferedImage.type) {
            BufferedImage.TYPE_3BYTE_BGR -> loadBgr((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_4BYTE_ABGR -> loadAbgr((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_BYTE_GRAY -> loadGray((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_USHORT_GRAY -> loadGray16((raster.dataBuffer as DataBufferUShort).data)
            else -> throw KorenderException("Unknown image format ${bufferedImage.type}")
        }
        val format = when (bufferedImage.type) {
            BufferedImage.TYPE_3BYTE_BGR -> GpuTexture.Format.RGB
            BufferedImage.TYPE_4BYTE_ABGR -> GpuTexture.Format.RGBA
            BufferedImage.TYPE_BYTE_GRAY -> GpuTexture.Format.Gray
            BufferedImage.TYPE_USHORT_GRAY -> GpuTexture.Format.Gray16
            else -> throw KorenderException("Unknown image format ${bufferedImage.type}")
        }
        return JvmImage(
            bufferedImage.raster,
            bufferedImage.width,
            bufferedImage.height,
            bytes,
            format
        )
    }

}

actual fun getPlatform(): Platform = JVMPlatform()

class JvmImage(
    private val raster: Raster,
    override val width: Int,
    override val height: Int,
    override val bytes: ByteBuffer,
    override val format: GpuTexture.Format
) : Image {

    private val pixel = FloatArray(3) { 0f }

    override fun pixel(x: Int, y: Int): com.zakgof.korender.math.Color {
        raster.getPixel(x, y, pixel)
        return when (format) {
            // TODO support more formats
            // TODO support transparency
            GpuTexture.Format.Gray16 -> com.zakgof.korender.math.Color(1.0f, pixel[0] / 65535.0f, pixel[0] / 65535.0f, pixel[0] / 65535.0f)
            else -> com.zakgof.korender.math.Color(1.0f, pixel[0] / 255.0f, pixel[1] / 255.0f, pixel[2] / 255.0f)
        }
    }
}

