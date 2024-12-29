package com.zakgof.korender.impl.buffer

import com.zakgof.korender.math.Vec3

expect class NativeByteBuffer(size: Int) {

    fun put(v: Float)

    fun put(v: Byte)

    fun put(v: Short)

    fun put(v: Int)

    fun put(rawBytes: ByteArray)

    fun int(index: Int): Int

    fun short(index: Int): Short

    fun byte(index: Int): Byte

    fun float(index: Int): Float

    fun rewind(): NativeByteBuffer

    fun put(other: NativeByteBuffer)

    fun size(): Int
}

fun NativeByteBuffer.put(v: Vec3) {
    put(v.x)
    put(v.y)
    put(v.z)
}

fun NativeByteBuffer.vec3(index: Int): Vec3 {
    val x = float(index * 3)
    val y = float(index * 3 + 1)
    val z = float(index * 3 + 2)
    return Vec3(x, y, z)
}

fun NativeByteBuffer.debugFloats(): String =
    (0 until size() / 4).take(1000).map { float(it) }.toString()

fun NativeByteBuffer.debugInts(): String =
    (0 until size() / 4).take(1000).map { int(it) }.toString()

fun NativeByteBuffer.debugShorts(): String =
    (0 until size() / 2).take(1000).map { short(it) }.toString()

