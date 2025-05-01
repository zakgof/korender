package com.zakgof.korender

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.opengl.GLSurfaceView
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
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


val androidContext = AtomicReference<Context>(null)

internal actual object Platform {

    actual val target = KorenderContext.TargetPlatform.Android

    internal actual fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage> =
        CompletableDeferred(bitmapToImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)))

    private fun bitmapToImage(bitmap: Bitmap): AndroidImage {
        val size = bitmap.rowBytes * bitmap.height
        val byteBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        bitmap.copyPixelsToBuffer(byteBuffer)
        val format = bitmap.config
        val gpuFormat = when (format) {
            Bitmap.Config.ARGB_8888 -> Image.Format.RGBA
            else -> throw KorenderException("Unsupported image format $format")
        }
        val gpuBytes = when (format) {
            Bitmap.Config.ARGB_8888 -> ARGBtoRGBA(byteBuffer) // TODO: how come ?
            else -> throw KorenderException("Unsupported image format $format")
        }
        return AndroidImage(
            bitmap.width,
            bitmap.height,
            NativeByteBuffer(gpuBytes),
            gpuFormat
        )
    }

    private fun ARGBtoRGBA(data: ByteBuffer): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(data.limit()).order(ByteOrder.nativeOrder())
        buffer.put(data.rewind() as ByteBuffer)
        return buffer.flip() as ByteBuffer
    }

    internal actual fun loadFont(bytes: ByteArray): Deferred<FontDef> {

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
        val texts = (0 until 128).map { "" + it.toChar() }
        val fontSize = 128f * cell.toFloat() / (paint.fontMetrics.descent - paint.fontMetrics.ascent)
        println("Effective font size $fontSize")
        paint.textSize = fontSize
        val widths = texts.mapIndexed { c, text ->
            val width = paint.measureText(text)
            canvas.drawText(
                "" + c.toChar(),
                ((c % 16) * cell).toFloat(),
                ((c / 16) * cell + cell - paint.fontMetrics.descent),
                paint
            )
            width / cell
        }.toFloatArray()
        val image = bitmapToImage(bitmap)
        return CompletableDeferred(FontDef(image, widths))
    }

    actual fun nanoTime() = System.nanoTime()
}

internal class AndroidImage(
    override val width: Int,
    override val height: Int,
    override val bytes: NativeByteBuffer,
    override val format: Image.Format
) : InternalImage

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
                        touch(TouchEvent(TouchEvent.Type.DOWN, TouchEvent.Button.LEFT, position.x, position.y))
                    }
                    if (event.type == PointerEventType.Release && !it.pressed && it.previousPressed) {
                        touch(TouchEvent(TouchEvent.Type.UP, TouchEvent.Button.LEFT, position.x, position.y))
                    }
                    if (event.type == PointerEventType.Move) {
                        touch(TouchEvent(TouchEvent.Type.MOVE, TouchEvent.Button.LEFT, position.x, position.y))
                    }
                }
            }
        }
    })


}

