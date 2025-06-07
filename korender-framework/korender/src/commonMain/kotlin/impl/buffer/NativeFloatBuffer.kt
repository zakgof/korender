package com.zakgof.korender.impl.buffer

expect class NativeFloatBuffer(size: Int) : NativeBuffer {
    fun put(v: Float)
    override fun rewind(): NativeFloatBuffer
    override fun size(): Int
    override fun position(): Int
    override fun position(offset: Int)
}