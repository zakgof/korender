package com.zakgof.korender.buffer

import java.nio.ByteBuffer
import java.nio.ByteOrder

actual object BufferUtils {

    // TODO clean
    actual fun createByteBuffer(capacity: Int) =
        Byter(ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()))

    actual fun createFloatBuffer(capacity: Int) =
        Floater(ByteBuffer.allocateDirect(capacity shl 2).order(ByteOrder.nativeOrder()))

    actual fun createIntBuffer(capacity: Int) =
        Inter(ByteBuffer.allocateDirect(capacity shl 2).order(ByteOrder.nativeOrder()))

    actual fun createShortBuffer(capacity: Int) =
        Shorter(ByteBuffer.allocateDirect(capacity shl 1).order(ByteOrder.nativeOrder()))
}
