package com.zakgof.korender.impl.buffer

import org.khronos.webgl.Float32Array
import org.khronos.webgl.set

actual class NativeFloatBuffer(override val array: Float32Array) : NativeBuffer {

    actual constructor(size: Int) : this(Float32Array(size))

    private var position = 0

    actual fun put(v: Float) {
        array[position++] = v
    }

    actual override fun rewind(): NativeFloatBuffer {
        position = 0
        return this
    }

    actual override fun size(): Int = array.length

    actual override fun position() = position

    actual override fun position(offset: Int) {
        position = offset;
    }
}