package com.zakgof.korender.impl.geometry

import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlin.experimental.and

internal enum class AttributeType {
    Byte,
    Short,
    Int,
    SignedByte,
    SignedShort,
    SignedInt,
    Float
}

internal interface BufferAccessor<T> {
    fun get(buffer: NativeByteBuffer, index: Int): T
    fun put(buffer: NativeByteBuffer, index: Int, value: T) {
        seek(buffer, index)
        put(buffer, value)
    }

    fun seek(buffer: NativeByteBuffer, index: Int)
    fun put(buffer: NativeByteBuffer, value: T)
    fun toByteArray(value: T): ByteArray
}

internal class InternalMeshAttribute<T>(
    override val name: String,
    override val structSize: Int,
    val primitiveType: AttributeType,
    val location: Int,
    val bufferAccessor: BufferAccessor<T>,
    val instance: Boolean = false,
) : MeshAttribute<T> {
    override fun toByteArray(value: T) = bufferAccessor.toByteArray(value)
}

internal object Vec2BufferAccessor : BufferAccessor<Vec2> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec2(buffer.float(index * 2), buffer.float(index * 2 + 1))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(8 * index)

    override fun put(buffer: NativeByteBuffer, value: Vec2) {
        buffer.put(value.x)
        buffer.put(value.y)
    }

    override fun toByteArray(value: Vec2): ByteArray {
        val ba = ByteArray(8)
        ba.setFloat(0, value.x)
        ba.setFloat(4, value.y)
        return ba
    }
}

internal object Vec3BufferAccessor : BufferAccessor<Vec3> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec3(buffer.float(index * 3), buffer.float(index * 3 + 1), buffer.float(index * 3 + 2))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(12 * index)

    override fun put(buffer: NativeByteBuffer, value: Vec3) {
        buffer.put(value.x)
        buffer.put(value.y)
        buffer.put(value.z)
    }

    override fun toByteArray(value: Vec3): ByteArray {
        val ba = ByteArray(12)
        ba.setFloat(0, value.x)
        ba.setFloat(4, value.y)
        ba.setFloat(8, value.z)
        return ba
    }
}

internal object Byte4BufferAccessor : BufferAccessor<ByteArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        byteArrayOf(buffer.byte(index), buffer.byte(index + 1), buffer.byte(index + 2), buffer.byte(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(4 * index)

    override fun put(buffer: NativeByteBuffer, value: ByteArray) {
        // TODO
    }

    override fun toByteArray(value: ByteArray) = value
}

internal object Short4BufferAccessor : BufferAccessor<ShortArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        shortArrayOf(buffer.short(index), buffer.short(index + 1), buffer.short(index + 2), buffer.short(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(8 * index)

    override fun put(buffer: NativeByteBuffer, value: ShortArray) {
        // TODO
    }

    override fun toByteArray(value: ShortArray): ByteArray {
        val ba = ByteArray(value.size * 2)
        value.forEachIndexed { index, sh -> ba.setShort(index * 2, sh) }
        return ba
    }
}

internal object Int4BufferAccessor : BufferAccessor<IntArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        intArrayOf(buffer.int(index), buffer.int(index + 1), buffer.int(index + 2), buffer.int(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(16 * index)

    override fun put(buffer: NativeByteBuffer, value: IntArray) {
        // TODO
    }

    override fun toByteArray(value: IntArray): ByteArray {
        val ba = ByteArray(4 * 4)
        value.forEachIndexed { index, i -> ba.setInt(index * 4, i) }
        return ba
    }
}

internal object Float4BufferAccessor : BufferAccessor<FloatArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        floatArrayOf(buffer.float(index), buffer.float(index + 1), buffer.float(index + 2), buffer.float(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(16 * index)

    override fun put(buffer: NativeByteBuffer, value: FloatArray) {
        value.forEach { buffer.put(it) }
    }

    override fun toByteArray(value: FloatArray): ByteArray {
        val ba = ByteArray(4 * 4)
        value.forEachIndexed { index, i -> ba.setFloat(index * 4, i) }
        return ba
    }
}

internal object ColorRGBABufferAccessor : BufferAccessor<ColorRGBA> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        ColorRGBA(buffer.float(index), buffer.float(index + 1), buffer.float(index + 2), buffer.float(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(16 * index)

    override fun put(buffer: NativeByteBuffer, value: ColorRGBA) {
        buffer.put(value.r)
        buffer.put(value.g)
        buffer.put(value.b)
        buffer.put(value.a)
    }

    override fun toByteArray(value: ColorRGBA): ByteArray {
        val ba = ByteArray(4 * 4)
        ba.setFloat(0, value.r)
        ba.setFloat(4, value.g)
        ba.setFloat(8, value.b)
        ba.setFloat(16, value.a)
        return ba
    }
}

internal object FloatBufferAccessor : BufferAccessor<Float> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.float(index)

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(index * 4)

    override fun put(buffer: NativeByteBuffer, value: Float) {
        buffer.put(value)
    }

    override fun toByteArray(value: Float): ByteArray {
        val ba = ByteArray(4)
        ba.setFloat(0, value)
        return ba
    }
}

internal object ByteBufferAccessor : BufferAccessor<Byte> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.byte(index)

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(index)

    override fun put(buffer: NativeByteBuffer, value: Byte) {
        buffer.put(value)
    }

    override fun toByteArray(value: Byte): ByteArray = ByteArray(1) { value }
}

internal fun ByteArray.setFloat(index: Int, v: Float) {
    setInt(index, v.toBits())
}

internal fun ByteArray.setInt(index: Int, v: Int) {
    this[index + 0] = (v and 0xFF).toByte()
    this[index + 1] = ((v shr 8) and 0xFF).toByte()
    this[index + 2] = ((v shr 16) and 0xFF).toByte()
    this[index + 3] = ((v shr 24) and 0xFF).toByte()
}

internal fun ByteArray.setShort(index: Int, v: Short) {
    this[index + 0] = (v and 0xFF).toByte()
    this[index + 1] = ((v.toInt() shr 8) and 0xFF).toByte()
}