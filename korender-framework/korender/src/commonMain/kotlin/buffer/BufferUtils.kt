package com.zakgof.korender.buffer

expect object BufferUtils {
    fun createByteBuffer(capacity: Int): Byter
    fun createIntBuffer(capacity: Int): Inter
    fun createFloatBuffer(capacity: Int): Floater
    fun createShortBuffer(capacity: Int): Shorter
}