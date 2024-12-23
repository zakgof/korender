package com.zakgof.korender.buffer

import org.khronos.webgl.Int16Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.khronos.webgl.toInt16Array

actual class Shorter(override val array : Int16Array) : BufferData<Short> {

    constructor(capacity: Int) : this(Int16Array(capacity))

    internal var position = 0

    actual fun put(values: ShortArray) {
        array.set(values.toInt16Array(), position)
        position += values.size
    }

    override fun rewind() {
        position = 0
    }

    override fun get(): Short = array[position++]

    override fun get(index: Int): Short = array[index]

    override fun position(): Int = position

    override fun position(pos: Int) {
        position = pos
    }

    override fun remaining(): Int = array.length - position

    override fun clear() {
        position = 0 // TODO check if this is needed
    }

    override fun put(value: Short) {
        array[position++] = value
    }

    override fun toString() = array.toString()

    override fun size() = array.length
}