package com.zakgof.korender.buffer

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
}

