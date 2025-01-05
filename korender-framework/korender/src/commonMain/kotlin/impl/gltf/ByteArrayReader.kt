package com.zakgof.korender.impl.gltf

import com.zakgof.korender.KorenderException

internal class ByteArrayReader(private val data: ByteArray) {

    private var position = 0

    fun readUInt32(): Int {
        val result = ((data[position].toInt() and 0xFF) or
                ((data[position + 1].toInt() and 0xFF) shl 8) or
                ((data[position + 2].toInt() and 0xFF) shl 16) or
                ((data[position + 3].toInt() and 0xFF) shl 24))
        position += 4
        return result
    }

    fun readBytes(length: Int): ByteArray {
        if (position + length > data.size) throw KorenderException("Not enough data to read")
        val result = data.copyOfRange(position, position + length)
        position += length
        return result
    }

    fun hasRemaining(): Boolean = position < data.size
}