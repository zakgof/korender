package com.zakgof.korender.buffer

import java.nio.ByteBuffer

actual class Shorter(override val byteBuffer: ByteBuffer) : BufferData<Short> {

    private val shortBuffer = byteBuffer.asShortBuffer()

    override fun rewind() {
        shortBuffer.rewind()
    }

    override fun get() = shortBuffer.get()

    override fun get(index: Int) = shortBuffer.get(index)

    override fun position(): Int = shortBuffer.position()

    override fun position(pos: Int) {
        shortBuffer.position(pos)
    }

    override fun remaining(): Int = shortBuffer.remaining()
    override fun clear() {
        shortBuffer.clear()
    }

    override fun put(value: Short) {
        shortBuffer.put(value)
    }

    actual fun put(values: ShortArray) {
        shortBuffer.put(values)
    }
}