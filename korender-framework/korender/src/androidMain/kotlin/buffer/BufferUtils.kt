package com.zakgof.korender.buffer

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BufferUtils {

    actual fun createByteBuffer(capacity: Int) =
        Byter(ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()))

    actual fun createFloatBuffer(capacity: Int) =
        Floater(ByteBuffer.allocateDirect(capacity shl 2).order(ByteOrder.nativeOrder()).asFloatBuffer())

    actual fun createIntBuffer(capacity: Int) =
        Inter(ByteBuffer.allocateDirect(capacity shl 2).order(ByteOrder.nativeOrder()).asIntBuffer())
}
