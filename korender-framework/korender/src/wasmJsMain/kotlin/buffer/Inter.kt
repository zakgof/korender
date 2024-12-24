package com.zakgof.korender.buffer

import org.khronos.webgl.Int32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.khronos.webgl.toInt32Array

actual class Inter(capacity: Int) : BufferData<Int> {

    override val array = Int32Array(capacity)
    private var position = 0

    actual fun put(values: IntArray) {
        array.set(values.toInt32Array(), position)
        position += values.size
    }

    override fun rewind() {
        position = 0
    }

    override fun get(): Int = array[position++]

    override fun get(index: Int): Int = array[index]

    override fun position(): Int = position

    override fun position(pos: Int) {
        position = pos
    }

    override fun remaining(): Int = array.length - position

    override fun clear() {
        position = 0 // TODO check if this is needed
    }

    override fun put(value: Int) {
        array[position++] = value
    }

    override fun toString() = array.toString()

    override fun size() = array.length
}