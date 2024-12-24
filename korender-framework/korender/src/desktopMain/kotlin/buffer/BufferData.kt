package com.zakgof.korender.buffer

import java.nio.ByteBuffer

actual interface BufferData<E> {
    val byteBuffer: ByteBuffer
    actual fun rewind()
    actual fun put(value: E)
    actual fun get(): E
    actual operator fun get(index: Int): E
    actual fun position(): Int
    actual fun position(pos: Int)
    actual fun remaining(): Int
    actual fun clear()
    actual fun size(): Int
}