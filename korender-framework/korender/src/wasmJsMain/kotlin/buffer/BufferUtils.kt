package com.zakgof.korender.buffer

actual object BufferUtils {

    actual fun createByteBuffer(capacity: Int): Byter = Byter(capacity)

    actual fun createIntBuffer(capacity: Int): Inter = Inter(capacity)

    actual fun createFloatBuffer(capacity: Int): Floater = Floater(capacity)

    actual fun createShortBuffer(capacity: Int): Shorter = Shorter(capacity)

}