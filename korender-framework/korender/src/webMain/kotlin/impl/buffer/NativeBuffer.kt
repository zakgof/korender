package com.zakgof.korender.impl.buffer

import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView


actual interface NativeBuffer {
    val array: ArrayBufferView<ArrayBuffer>
    actual fun rewind(): NativeBuffer
    actual fun size(): Int
    actual fun position(): Int
    actual fun position(offset: Int)
}