package com.zakgof.korender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.gl.VGL12
import com.zakgof.korender.gl.VGL13
import com.zakgof.korender.gl.VGL14
import com.zakgof.korender.gl.VGL15
import com.zakgof.korender.gl.VGL20
import com.zakgof.korender.gl.VGL30
import com.zakgof.korender.gles.Gles11
import com.zakgof.korender.gles.Gles12
import com.zakgof.korender.gles.Gles13
import com.zakgof.korender.gles.Gles14
import com.zakgof.korender.gles.Gles15
import com.zakgof.korender.gles.Gles20
import com.zakgof.korender.gles.Gles30
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.Image
import java.io.InputStream
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    @Composable
    override fun openGL(init: () -> Unit, frame: () -> Unit, resize: (Int, Int) -> Unit) {
        AndroidView(factory = { KorenderGLSurfaceView(it, init, frame, resize) })
    }

    override fun loadImage(stream: InputStream): Image {
        val bitmap = BitmapFactory.decodeStream(stream)
        val size = bitmap.rowBytes * bitmap.height
        val byteBuffer = BufferUtils.createByteBuffer(size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        val format = bitmap.config
        val gpuFormat = when(format) {
            Bitmap.Config.ARGB_8888 -> GpuTexture.Format.RGBA
            else -> throw KorenderException("Unsupported image format $format")
        }
        val gpuBytes = when(format) {
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
        val buffer = BufferUtils.createByteBuffer(data.limit())
        for (i in 0 until data.limit() / 4) {
            buffer.put(data[i * 4 + 0])
                .put(data[i * 4 + 1])
                .put(data[i * 4 + 2])
                .put(data[i * 4 + 3])
        }
        return buffer.flip() as ByteBuffer
    }
}

class AndroidImage(
    override val width: Int, override val height: Int,
    override val bytes: ByteBuffer, override val format: GpuTexture.Format
) : Image


actual fun getPlatform(): Platform = AndroidPlatform()

class KorenderGLSurfaceView(
    context: Context,
    init: () -> Unit,
    frame: () -> Unit,
    resize: (Int, Int) -> Unit
) : GLSurfaceView(context) {

    private val renderer: KorenderGLRenderer

    init {

        VGL11.gl = Gles11
        VGL12.gl = Gles12
        VGL13.gl = Gles13
        VGL14.gl = Gles14
        VGL15.gl = Gles15
        VGL20.gl = Gles20
        VGL30.gl = Gles30

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)

        renderer = KorenderGLRenderer(init, frame, resize)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }
}

class KorenderGLRenderer(
    private val init: () -> Unit,
    private val frame: () -> Unit,
    private val resize: (Int, Int) -> Unit
) :
    GLSurfaceView.Renderer {

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) = init()

    override fun onDrawFrame(unused: GL10) = frame()

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) = resize(width, height)
}