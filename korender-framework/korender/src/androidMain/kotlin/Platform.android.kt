package com.zakgof.korender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.zakgof.korender.gles.Gles11
import com.zakgof.korender.gles.Gles12
import com.zakgof.korender.gles.Gles13
import com.zakgof.korender.gles.Gles14
import com.zakgof.korender.gles.Gles15
import com.zakgof.korender.gles.Gles20
import com.zakgof.korender.gles.Gles30
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.input.TouchEvent
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max

val androidContext = AtomicReference<Context>(null)

class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    @Composable
    override fun openGL(
        init: (Int, Int) -> Unit,
        frame: () -> Unit,
        resize: (Int, Int) -> Unit,
        touch: (touchEvent: TouchEvent) -> Unit
    ) {
        AndroidView(factory = {
            LinearLayout(it).apply {
                addView(KorenderGLSurfaceView(it, init, frame, resize))
                layoutParams = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT
                )
            }
        }, modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.changes.isNotEmpty()) {
                        val position = event.changes[0].position
                        if (event.type == PointerEventType.Press) {
                            touch(TouchEvent(TouchEvent.Type.DOWN, position.x, position.y))
                        }
                        if (event.type == PointerEventType.Release) {
                            touch(TouchEvent(TouchEvent.Type.UP, position.x, position.y))
                        }
                        if (event.type == PointerEventType.Move) {
                            touch(TouchEvent(TouchEvent.Type.MOVE, position.x, position.y))
                        }
                    }
                }
            }
        })
    }

    override fun loadImage(stream: InputStream): Image {
        val bitmap = BitmapFactory.decodeStream(stream)
        return bitmapToImage(bitmap)
    }

    private fun bitmapToImage(bitmap: Bitmap): AndroidImage {
        val size = bitmap.rowBytes * bitmap.height
        val byteBuffer = com.zakgof.korender.impl.glgpu.BufferUtils.createByteBuffer(size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        val format = bitmap.config
        val gpuFormat = when (format) {
            Bitmap.Config.ARGB_8888 -> GpuTexture.Format.RGBA
            else -> throw KorenderException("Unsupported image format $format")
        }
        val gpuBytes = when (format) {
            Bitmap.Config.ARGB_8888 -> ARGBtoRGBA(byteBuffer) // TODO: how come ?
            else -> throw KorenderException("Unsupported image format $format")
        }
        return AndroidImage(
            bitmap.width,
            bitmap.height,
            gpuBytes,
            gpuFormat
        )
    }

    private fun ARGBtoRGBA(data: ByteBuffer): ByteBuffer {
        val buffer = com.zakgof.korender.impl.glgpu.BufferUtils.createByteBuffer(data.limit())
        buffer.put(data.rewind() as ByteBuffer)
        return buffer.flip() as ByteBuffer
    }

    override fun loadFont(stream: InputStream): FontDef {

        val tmpDir: File = androidContext.get().cacheDir
        val tmpFile = File.createTempFile("font-", ".ttf", tmpDir)
        tmpFile.outputStream().use { fileOut ->
            stream.copyTo(fileOut)
        }

        val cell = 64
        val bitmap = Bitmap.createBitmap(cell * 16, cell * 16, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.typeface = Typeface.createFromFile(tmpFile)
        paint.color = Color.WHITE
        paint.textSize = 128f
        val bounds = Rect()

        val maxwidthheight =
            (0 until 128).maxOf {
                paint.getTextBounds("" + it.toChar(), 0, 1, bounds)
                max(bounds.right, bounds.height() )
            }
        val fontSize = 128f * cell.toFloat() / maxwidthheight
        paint.textSize = fontSize

        val widths = FloatArray(128)
        for (c in 0 until 128) {
            val width = paint.measureText("" + c.toChar())
            widths[c] = width / cell
            canvas.drawText(
                "" + c.toChar(),
                ((c % 16) * cell).toFloat(),
                ((c / 16) * cell + cell - paint.fontMetrics.descent),
                paint
            )
        }
        val image = bitmapToImage(bitmap)
        return FontDef(image, widths)
    }
}

class AndroidImage(
    override val width: Int, override val height: Int,
    override val bytes: ByteBuffer, override val format: GpuTexture.Format
) : Image


actual fun getPlatform(): Platform = AndroidPlatform()

class KorenderGLSurfaceView(
    context: Context,
    init: (Int, Int) -> Unit,
    frame: () -> Unit,
    resize: (Int, Int) -> Unit
) : GLSurfaceView(context) {

    private val renderer: KorenderGLRenderer

    init {

        androidContext.set(context)

        layoutParams = LinearLayout.LayoutParams(
            MATCH_PARENT, MATCH_PARENT
        )

        com.zakgof.korender.impl.gl.VGL11.gl = Gles11
        com.zakgof.korender.impl.gl.VGL12.gl = Gles12
        com.zakgof.korender.impl.gl.VGL13.gl = Gles13
        com.zakgof.korender.impl.gl.VGL14.gl = Gles14
        com.zakgof.korender.impl.gl.VGL15.gl = Gles15
        com.zakgof.korender.impl.gl.VGL20.gl = Gles20
        com.zakgof.korender.impl.gl.VGL30.gl = Gles30

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)

        renderer = KorenderGLRenderer(init, frame, resize, this)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }
}

class KorenderGLRenderer(
    private val init: (Int, Int) -> Unit,
    private val frame: () -> Unit,
    private val resize: (Int, Int) -> Unit,
    private val view: View
) :
    GLSurfaceView.Renderer {

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) = init(view.width, view.height)

    override fun onDrawFrame(unused: GL10) = frame()

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) = resize(width, height)
}