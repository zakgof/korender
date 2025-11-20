package com.zakgof.korender.impl.buffer

import org.khronos.webgl.ArrayBufferView

actual interface NativeBuffer {
    val array: ArrayBufferView
    actual fun rewind(): NativeBuffer
    actual fun size(): Int
    actual fun position(): Int
    actual fun position(offset: Int)
}