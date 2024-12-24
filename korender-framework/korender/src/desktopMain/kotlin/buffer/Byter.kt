package com.zakgof.korender.buffer

import java.nio.ByteBuffer

actual class Byter(override val byteBuffer: ByteBuffer) : BufferData<Byte> {

    override fun rewind() {
        byteBuffer.rewind()
    }

    override fun get() = byteBuffer.get()

    override fun get(index: Int) = byteBuffer.get(index)

    override fun position(): Int = byteBuffer.position()

    override fun position(pos: Int) {
        byteBuffer.position(pos)
    }

    override fun remaining(): Int = byteBuffer.remaining()
    override fun clear() {
        byteBuffer.clear()
    }

    override fun put(value: Byte) {
        byteBuffer.put(value)
    }

    actual fun put(values: ByteArray) {
        byteBuffer.put(values)
    }

    actual fun toFloater(): Floater = Floater(byteBuffer)

    override fun size() = byteBuffer.limit()
}