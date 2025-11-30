package com.zakgof.korender.examples.island

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

fun <T> loadBinary(bytes: ByteArray, block: BinaryLoaderContext.() -> T): T {
    val context = BinaryLoaderContext(bytes)
    return block.invoke(context)
}

class BinaryLoaderContext(val bytes: ByteArray) {

    private var offset = 0

    fun getInt(): Int {
        val bits = (bytes[offset + 3].toInt() and 0xFF shl 24) or
                (bytes[offset + 2].toInt() and 0xFF shl 16) or
                (bytes[offset + 1].toInt() and 0xFF shl 8) or
                (bytes[offset].toInt() and 0xFF)
        offset += 4
        return bits
    }

    fun getFloat() = Float.fromBits(getInt())

    fun getVec3(): Vec3 {
        val x = getFloat()
        val y = getFloat()
        val z = getFloat()
        return Vec3(x, y, z)
    }

    fun getVec2(): Vec2 {
        val x = getFloat()
        val y = getFloat()
        return Vec2(x, y)
    }

}