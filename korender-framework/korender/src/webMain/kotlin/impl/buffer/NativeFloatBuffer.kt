package com.zakgof.korender.impl.buffer

import js.buffer.ArrayBuffer
import js.core.JsPrimitives.toJsFloat
import js.typedarrays.Float32Array

actual class NativeFloatBuffer(override val array: Float32Array<ArrayBuffer>) : NativeBuffer {

    actual constructor(size: Int) : this(Float32Array(size))

    private var position = 0

    actual fun put(v: Float) {
        array[position++] = v.toJsFloat()
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