package com.zakgof.korender

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.image.Image
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.input.TouchEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max

val androidContext = AtomicReference<Context>(null)

actual object Platform {

    actual val name: String = "Android ${Build.VERSION.SDK_INT}"

    actual fun loadImage(bytes: ByteArray, type: String): Deferred<Image> =
        CompletableDeferred(bitmapToImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)))

    private fun bitmapToImage(bitmap: Bitmap): AndroidImage {
        val size = bitmap.rowBytes * bitmap.height
        val byteBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        bitmap.copyPixelsToBuffer(byteBuffer)
        val format = bitmap.config
        val gpuFormat = when (format) {
            Bitmap.Config.ARGB_8888 -> GlGpuTexture.Format.RGBA
            else -> throw KorenderException("Unsupported image format $format")
        }
        val gpuBytes = when (format) {
            Bitmap.Config.ARGB_8888 -> ARGBtoRGBA(byteBuffer) // TODO: how come ?
            else -> throw KorenderException("Unsupported image format $format")
        }
        return AndroidImage(
            bitmap,
            bitmap.width,
            bitmap.height,
            Byter(gpuBytes),
            gpuFormat
        )
    }

    private fun ARGBtoRGBA(data: ByteBuffer): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(data.limit()).order(ByteOrder.nativeOrder())
        buffer.put(data.rewind() as ByteBuffer)
        return buffer.flip() as ByteBuffer
    }

    actual fun loadFont(bytes: ByteArray): Deferred<FontDef> {

        val tmpDir: File = androidContext.get().cacheDir
        val tmpFile = File.createTempFile("font-", ".ttf", tmpDir)
        tmpFile.writeBytes(bytes)

        val cell = 256
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
                max(bounds.right, bounds.height())
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
        return CompletableDeferred(FontDef(image, widths))
    }

    actual fun nanoTime() = System.nanoTime()
}

class AndroidImage(
    private val bitmap: Bitmap,
    override val width: Int,
    override val height: Int,
    override val bytes: Byter,
    override val format: GlGpuTexture.Format
) : Image {
    override fun pixel(x: Int, y: Int): com.zakgof.korender.math.Color {
        // TODO: performance optimization
        val androidColor = bitmap.getPixel(x, y)
        return com.zakgof.korender.math.Color(androidColor.toLong())
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
actual fun Korender(appResourceLoader: ResourceLoader, block: KorenderContext.() -> Unit) {

    var engine: Engine? by remember { mutableStateOf(null) }

    class KorenderGLRenderer(private val view: View) : GLSurfaceView.Renderer {

        override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
            val async = object : AsyncContext {
                override val appResourceLoader = appResourceLoader
                override fun <R> call(function: suspend () -> R): Deferred<R> =
                    CompletableDeferred(runBlocking { function() })
            }
            engine = Engine(view.width, view.height, async, block)
        }

        override fun onDrawFrame(unused: GL10) {
            engine?.frame()
        }

        override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
            engine?.resize(width, height)
        }
    }

    @SuppressLint("ViewConstructor")
    class KorenderGLSurfaceView(context: Context) : GLSurfaceView(context) {
        init {
            androidContext.set(context)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setEGLContextClientVersion(3)
            setRenderer(KorenderGLRenderer(this))
        }
    }

    fun touch(event: TouchEvent) = GlobalScope.launch { engine?.pushTouch(event) }

    AndroidView(factory = {
        LinearLayout(it).apply {
            addView(KorenderGLSurfaceView(it))
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }, modifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                println("Event type ${event.type} Changes: ${event.changes}")
                event.changes.forEach {
                    val position = it.position
                    if (event.type == PointerEventType.Press && it.pressed && !it.previousPressed) {
                        touch(TouchEvent(TouchEvent.Type.DOWN, position.x, position.y))
                    }
                    if (event.type == PointerEventType.Release && !it.pressed && it.previousPressed) {
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

