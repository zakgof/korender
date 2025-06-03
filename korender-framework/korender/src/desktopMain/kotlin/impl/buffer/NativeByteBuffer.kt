package com.zakgof.korender.impl.buffer

import java.nio.ByteBuffer
import java.nio.ByteOrder

actual class NativeByteBuffer(val byteBuffer: ByteBuffer) {

    actual constructor(size: Int) :
            this(ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN))

    actual operator fun set(index: Int, byte: Byte) {
        byteBuffer.put(index, byte)
    }

    actual fun put(v: Float) {
        val intBits = v.toBits()
        put(intBits)
    }

    actual fun put(v: Byte) {
        byteBuffer.put(v)
    }

    actual fun put(v: Short) {
        val vi = v.toInt()
        byteBuffer.put((vi and 0xFF).toByte())
        byteBuffer.put(((vi shr 8) and 0xFF).toByte())
    }

    actual fun put(v: Int) {
        byteBuffer.put((v and 0xFF).toByte())
        byteBuffer.put(((v shr 8) and 0xFF).toByte())
        byteBuffer.put(((v shr 16) and 0xFF).toByte())
        byteBuffer.put(((v shr 24) and 0xFF).toByte())
    }

    actual fun put(rawBytes: ByteArray) {
        byteBuffer.put(rawBytes)
    }

    actual fun put(other: NativeByteBuffer) {
        byteBuffer.put(other.byteBuffer)
    }

    actual fun int(index: Int) =
        (byteBuffer[index * 4 + 0].toInt() and 0xFF) or
                ((byteBuffer[index * 4 + 1].toInt() and 0xFF) shl 8) or
                ((byteBuffer[index * 4 + 2].toInt() and 0xFF) shl 16) or
                ((byteBuffer[index * 4 + 3].toInt() and 0xFF) shl 24)

    actual fun short(index: Int) =
        ((byteBuffer[index * 2].toInt() and 0xFF) or
                ((byteBuffer[index * 2 + 1].toInt() and 0xFF) shl 8)
                ).toShort()

    actual fun float(index: Int) =
        Float.fromBits(int(index))

    actual fun byte(index: Int) =
        byteBuffer[index]

    actual fun rewind(): NativeByteBuffer {
        byteBuffer.rewind()
        return this
    }

    actual fun size() = byteBuffer.limit()

    actual fun position() = byteBuffer.position()

    actual fun position(offset: Int) {
        byteBuffer.position(offset)
    }
}