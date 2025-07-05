package com.zakgof.korender.impl.buffer

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

actual class NativeFloatBuffer(override val byteBuffer: ByteBuffer) : NativeBuffer {

    private val floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer()

    actual constructor(size: Int) :
            this(ByteBuffer.allocateDirect(size * 4).order(ByteOrder.LITTLE_ENDIAN))

    actual fun put(v: Float) {
        floatBuffer.put(v)
    }

    actual override fun size() = floatBuffer.limit()

    actual override fun position() = floatBuffer.position()

    actual override fun position(offset: Int) {
        floatBuffer.position(offset)
    }

    actual override fun rewind(): NativeFloatBuffer {
        floatBuffer.rewind()
        return this
    }
}