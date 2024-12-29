package com.zakgof.korender.impl.buffer

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.khronos.webgl.toUint8Array

actual class NativeByteBuffer actual constructor(size: Int) {

    constructor(byteArray: ByteArray) : this(byteArray.size) {
        put(byteArray)
        rewind()
    }

    internal val array = Uint8Array(size)
    private var position = 0

    actual fun put(v: Byte) {
        array[position++] = v
    }

    actual fun put(v: Short) {
        val vi = v.toInt()
        put((vi and 0xFF).toByte())
        put(((vi shr 8) and 0xFF).toByte())
    }

    actual fun put(v: Int) {
        put((v and 0xFF).toByte())
        put(((v shr 8) and 0xFF).toByte())
        put(((v shr 16) and 0xFF).toByte())
        put(((v shr 24) and 0xFF).toByte())
    }

    actual fun put(v: Float) {
        put(v.toBits())
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    actual fun put(rawBytes: ByteArray) {
        array.set(rawBytes.asUByteArray().toUint8Array(), position)
        position += rawBytes.size
    }

    actual fun int(index: Int): Int {
        return (array[index * 4 + 0].toInt() and 0xFF) or
                ((array[index * 4 + 1].toInt() and 0xFF) shl 8) or
                ((array[index * 4 + 2].toInt() and 0xFF) shl 16) or
                ((array[index * 4 + 3].toInt() and 0xFF) shl 24)
    }

    actual fun short(index: Int): Short {
        return ((array[index * 2].toInt() and 0xFF) or
                ((array[index * 2 + 1].toInt() and 0xFF) shl 8)
                ).toShort()
    }

    actual fun byte(index: Int): Byte {
        return array[index]
    }

    actual fun float(index: Int): Float {
        return Float.fromBits(int(index))
    }

    actual fun rewind(): NativeByteBuffer {
        position = 0
        return this
    }

    actual fun put(other: NativeByteBuffer) {
        array.set(other.array, position)
    }

    actual fun size(): Int = array.length

}