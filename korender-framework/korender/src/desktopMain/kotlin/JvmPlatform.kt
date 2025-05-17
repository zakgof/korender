package com.zakgof.korender

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.image.InternalImage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import java.awt.event.MouseEvent.BUTTON3
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferUShort
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.math.max
import kotlin.math.round

private fun detectDevicePixelRatio(): List<Float> {
    val device = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    val scaleX = device::class.members.firstOrNull { it.name == "getDefaultScaleX" }
        ?.call(device) as Float?
    val scaleY = device::class.members.firstOrNull { it.name == "getDefaultScaleY" }
        ?.call(device) as Float?
    return listOf(scaleX ?: 1.0f, scaleY ?: 1.0f)
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
actual fun Korender(
    appResourceLoader: ResourceLoader,
    block: KorenderContext.() -> Unit
) {
    var engine: Engine? by remember { mutableStateOf(null) }
    val pixelRatio by remember { mutableStateOf(detectDevicePixelRatio()) }

    fun sendTouch(
        type: TouchEvent.Type,
        button: TouchEvent.Button,
        ex: Int,
        ey: Int
    ) {
        GlobalScope.launch {
            engine?.pushTouch(TouchEvent(type, button, ex * pixelRatio[0], ey * pixelRatio[1]))
        }
    }

    fun sendKey(
        type: com.zakgof.korender.KeyEvent.Type,
        c: String
    ) {
        GlobalScope.launch {
            engine?.pushKey(KeyEvent(type, c))
        }
    }

    SwingPanel(
        modifier = Modifier.fillMaxSize(),
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
            val data = GLData()
            data.swapInterval = 0
            data.majorVersion = 3
            data.minorVersion = 0
            // TODO
            data.samples = 1

            val canvas = object : AWTGLCanvas(data) {

                private fun <R> asyncInContext(function: suspend () -> R): Deferred<R> =
                    CompletableDeferred(runBlocking {
                        function()
                    })

                override fun initGL() {
                    println("OpenGL version: ${effective.majorVersion}.${effective.minorVersion} (Profile: ${effective.profile})")
                    createCapabilities()

                    val async = object : AsyncContext {
                        override val appResourceLoader = appResourceLoader
                        override fun <R> call(function: suspend () -> R): Deferred<R> =
                            asyncInContext(function)
                    }

                    engine = Engine(
                        (this.size.width * pixelRatio[0]).toInt(),
                        (this.size.height * pixelRatio[1]).toInt(),
                        async,
                        block
                    )
                }

                override fun paintGL() {
                    engine?.frame()
                    swapBuffers()
                }
            }

            fun Int.toButton(): TouchEvent.Button = when (this) {
                BUTTON1 -> TouchEvent.Button.LEFT
                BUTTON3 -> TouchEvent.Button.RIGHT
                else -> TouchEvent.Button.NONE
            }

            canvas.addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) =
                    sendTouch(TouchEvent.Type.MOVE, e.button.toButton(), e.x, e.y)

                override fun mouseDragged(e: MouseEvent) =
                    sendTouch(TouchEvent.Type.MOVE, e.button.toButton(), e.x, e.y)
            })
            canvas.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) =
                    sendTouch(TouchEvent.Type.DOWN, e.button.toButton(), e.x, e.y)

                override fun mouseReleased(e: MouseEvent) =
                    sendTouch(TouchEvent.Type.UP, e.button.toButton(), e.x, e.y)
            })
            canvas.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    sendKey(com.zakgof.korender.KeyEvent.Type.DOWN, e.keyChar.toString()) // TODO all keycodes
                }

                override fun keyReleased(e: KeyEvent) {
                    sendKey(com.zakgof.korender.KeyEvent.Type.UP, e.keyChar.toString()) // TODO all keycodes
                }
            })

            canvas.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent?) {
                    canvas.runInContext {
                        engine?.resize(
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

internal actual object Platform {

    actual val target = KorenderContext.TargetPlatform.Desktop

    actual fun nanoTime() = System.nanoTime()

    internal actual fun createImage(width: Int, height: Int, format: Image.Format) =
        image(BufferedImage(width, height, format.toBufferedImageType()))

    internal actual fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage> =
        CompletableDeferred(image(ImageIO.read(ByteArrayInputStream(bytes))))

    private fun loadBgr(data: ByteArray): NativeByteBuffer {
        val buffer = NativeByteBuffer(data.size)
        for (i in 0 until data.size / 3) {
            buffer.byteBuffer.put(data[i * 3 + 2])
                .put(data[i * 3 + 1])
                .put(data[i * 3])
        }
        buffer.byteBuffer.flip()
        return buffer
    }

    private fun loadGray(data: ByteArray): NativeByteBuffer {
        val buffer = NativeByteBuffer(data.size)
        buffer.byteBuffer.put(data).flip()
        return buffer
    }

    // TODO: test
    private fun loadGray16(data: ShortArray): NativeByteBuffer {
        val bb = NativeByteBuffer(data.size * 2)
        val buffer = bb.byteBuffer.asShortBuffer()
        buffer.put(data).flip()
        return bb
    }

    private fun loadAbgr(data: ByteArray): NativeByteBuffer {
        val buffer = NativeByteBuffer(data.size)
        for (i in 0 until data.size / 4) {
            buffer.byteBuffer.put(data[i * 4 + 3])
                .put(data[i * 4 + 2])
                .put(data[i * 4 + 1])
                .put(data[i * 4])
        }
        buffer.byteBuffer.flip()
        return buffer
    }

    internal actual fun loadFont(bytes: ByteArray): Deferred<FontDef> {
        val cell = 256
        val originalFont = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(bytes))
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
        return CompletableDeferred(FontDef(image, widths))
    }

    private fun image(bufferedImage: BufferedImage): InternalImage {
        val raster = bufferedImage.raster
        val bytes = when (bufferedImage.type) {
            BufferedImage.TYPE_3BYTE_BGR -> loadBgr((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_4BYTE_ABGR -> loadAbgr((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_BYTE_GRAY -> loadGray((raster.dataBuffer as DataBufferByte).data)
            BufferedImage.TYPE_USHORT_GRAY -> loadGray16((raster.dataBuffer as DataBufferUShort).data)
            else -> throw KorenderException("Unknown image format ${bufferedImage.type}")
        }
        val format = when (bufferedImage.type) {
            BufferedImage.TYPE_3BYTE_BGR -> Image.Format.RGB
            BufferedImage.TYPE_4BYTE_ABGR -> Image.Format.RGBA
            BufferedImage.TYPE_BYTE_GRAY -> Image.Format.Gray
            BufferedImage.TYPE_USHORT_GRAY -> Image.Format.Gray16
            else -> throw KorenderException("Unknown image format ${bufferedImage.type}")
        }
        return InternalImage(
            bufferedImage.width,
            bufferedImage.height,
            bytes,
            format
        )
    }
}

fun Image.Format.toBufferedImageType() = when (this) {
    Image.Format.RGB -> BufferedImage.TYPE_3BYTE_BGR
    Image.Format.RGBA -> BufferedImage.TYPE_4BYTE_ABGR
    Image.Format.Gray -> BufferedImage.TYPE_BYTE_GRAY
    Image.Format.Gray16 -> BufferedImage.TYPE_USHORT_GRAY
}

