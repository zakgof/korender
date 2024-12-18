package com.zakgof.korender.buffer

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.khronos.webgl.toInt8Array

actual class Byter(capacity: Int) : BufferData<Byte> {

    override val array = Int8Array(capacity)
    internal var position = 0

    actual fun put(values: ByteArray) {
        array.set(values.toInt8Array(), position)
        position += values.size
    }

    override fun rewind() {
        position = 0
    }

    override fun get(): Byte = array[position++]

    override fun get(index: Int): Byte = array[index]

    override fun position(): Int = position

    override fun position(pos: Int) {
        position = pos
    }

    override fun remaining(): Int = array.length - position

    override fun clear() {
        position = 0 // TODO check if this is needed
    }

    override fun put(value: Byte) {
        array[position++] = value
    }

    override fun toString() = array.toString()

}