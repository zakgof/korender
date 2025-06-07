package com.zakgof.korender.impl.buffer

expect interface NativeBuffer {
    fun rewind(): NativeBuffer
    fun size(): Int
    fun position(): Int
    fun position(offset: Int)
}