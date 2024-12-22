package com.zakgof.korender.buffer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.khronos.webgl.toFloat32Array

actual class Floater(capacity: Int) : BufferData<Float> {

    override val array = Float32Array(capacity)
    internal var position = 0

    actual fun put(values: FloatArray) {
        array.set(values.toFloat32Array(), position)
        position += values.size
    }

    override fun rewind() {
        position = 0
    }

    override fun get(): Float = array[position++]

    override fun get(index: Int): Float = array[index]

    override fun position(): Int = position

    override fun position(pos: Int) {
        position = pos
    }

    override fun remaining(): Int = array.length - position

    override fun clear() {
        position = 0 // TODO check if this is needed
    }

    override fun put(value: Float) {
        array[position++] = value
    }

    override fun toString() = array.toString()

}