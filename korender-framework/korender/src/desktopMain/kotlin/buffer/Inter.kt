package com.zakgof.korender.buffer

import java.nio.ByteBuffer

actual class Inter(override val byteBuffer: ByteBuffer) : BufferData<Int> {

    val intBuffer = byteBuffer.asIntBuffer()

    override fun rewind() {
        intBuffer.rewind()
    }

    override fun get() = intBuffer.get()

    override fun get(index: Int) = intBuffer.get(index)

    override fun position(): Int = intBuffer.position()

    override fun position(pos: Int) {
        intBuffer.position(pos)
    }

    override fun remaining(): Int = intBuffer.remaining()
    override fun clear() {
        intBuffer.clear()
    }

    override fun put(value: Int) {
        intBuffer.put(value)
    }

    actual fun put(values: IntArray) {
        intBuffer.put(values)
    }

    override fun size() = intBuffer.limit()
}