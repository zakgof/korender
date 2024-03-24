package com.zakgof.korender.impl.glgpu

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

object BufferUtils {

    fun createByteBuffer(capacity: Int): ByteBuffer {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())
    }

    fun createFloatBuffer(capacity: Int): FloatBuffer {
        return createByteBuffer(capacity shl 2).asFloatBuffer()
    }

    fun createIntBuffer(capacity: Int): IntBuffer {
        return createByteBuffer(capacity shl 2).asIntBuffer()
    }
}