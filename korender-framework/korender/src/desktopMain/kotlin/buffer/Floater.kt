package com.zakgof.korender.buffer

import java.nio.ByteBuffer
import java.nio.FloatBuffer

actual class Floater(override val byteBuffer: ByteBuffer) : BufferData<Float> {

    val floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer()

    override fun rewind() {
        floatBuffer.rewind()
    }

    override fun get() = floatBuffer.get()

    override fun get(index: Int) = floatBuffer.get(index)

    override fun position(): Int = floatBuffer.position()

    override fun position(pos: Int) {
        floatBuffer.position(pos)
    }

    override fun remaining(): Int = floatBuffer.remaining()
    override fun clear() {
        floatBuffer.clear()
    }

    override fun put(value: Float) {
        floatBuffer.put(value)
    }

    actual fun put(values: FloatArray) {
        floatBuffer.put(values)
    }

    override fun size() = floatBuffer.limit()
}