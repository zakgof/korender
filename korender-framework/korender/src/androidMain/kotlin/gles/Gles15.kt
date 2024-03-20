package com.zakgof.korender.gles

import android.opengl.GLES20
import com.zakgof.korender.gl.IGL15
import java.nio.ByteBuffer

object Gles15 : IGL15 {

    override fun glBindBuffer(target: Int, buffer: Int) = GLES20.glBindBuffer(target, buffer)

    override fun glBufferData(target: Int, data: ByteBuffer, usage: Int) =
        GLES20.glBufferData(target, data.remaining(), data, usage)

    override fun glGenBuffers(): Int {
        val array = IntArray(1)
        GLES20.glGenBuffers(1, array, 0)
        return array[0]
    }

    override fun glDeleteBuffers(buffer: Int) = GLES20.glDeleteBuffers(1, intArrayOf(buffer), 0)
}
