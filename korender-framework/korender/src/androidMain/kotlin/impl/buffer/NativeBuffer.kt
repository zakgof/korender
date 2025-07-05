package com.zakgof.korender.impl.buffer

import java.nio.ByteBuffer;

actual interface NativeBuffer {
    val byteBuffer: ByteBuffer
    actual fun rewind(): NativeBuffer
    actual fun size(): Int
    actual fun position(): Int
    actual fun position(offset: Int)
}
