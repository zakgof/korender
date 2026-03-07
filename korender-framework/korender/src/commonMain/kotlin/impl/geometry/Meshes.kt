package com.zakgof.korender.impl.geometry

import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

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
    fun put(buffer: NativeByteBuffer, index: Int, value: T)
}

internal class InternalMeshAttribute<T>(
    val name: String,
    val structSize: Int,
    val primitiveType: AttributeType,
    val location: Int,
    val bufferAccessor: BufferAccessor<T>,
    val instance: Boolean = false,
) : MeshAttribute<T>

internal object Vec2BufferAccessor : BufferAccessor<Vec2> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec2(buffer.float(index * 2), buffer.float(index * 2 + 1))

    override fun put(buffer: NativeByteBuffer, index: Int, value: Vec2) {
        buffer.position(8 * index)
        buffer.put(value.x)
        buffer.put(value.y)
    }
}

internal object Vec3BufferAccessor : BufferAccessor<Vec3> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec3(buffer.float(index * 3), buffer.float(index * 3 + 1), buffer.float(index * 3 + 2))

    override fun put(buffer: NativeByteBuffer, index: Int, value: Vec3) {
        buffer.position(12 * index)
        buffer.put(value.x)
        buffer.put(value.y)
        buffer.put(value.z)
    }
}

internal object Byte4BufferAccessor : BufferAccessor<ByteArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        byteArrayOf(buffer.byte(index), buffer.byte(index + 1), buffer.byte(index + 2), buffer.byte(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: ByteArray) {
        // TODO
    }
}

internal object Short4BufferAccessor : BufferAccessor<ShortArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        shortArrayOf(buffer.short(index), buffer.short(index + 1), buffer.short(index + 2), buffer.short(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: ShortArray) {
        // TODO
    }
}

internal object Int4BufferAccessor : BufferAccessor<IntArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        intArrayOf(buffer.int(index), buffer.int(index + 1), buffer.int(index + 2), buffer.int(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: IntArray) {
        // TODO
    }
}

internal object Float4BufferAccessor : BufferAccessor<FloatArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        floatArrayOf(buffer.float(index), buffer.float(index + 1), buffer.float(index + 2), buffer.float(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: FloatArray) {
        buffer.position(4 * 4 * index)
        value.forEach { buffer.put(it) }
    }
}

internal object FloatBufferAccessor : BufferAccessor<Float> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.float(index)

    override fun put(buffer: NativeByteBuffer, index: Int, value: Float) {
        buffer.position(index * 4)
        buffer.put(value)
    }
}

object ByteBufferAccessor : BufferAccessor<Byte> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.byte(index)

    override fun put(buffer: NativeByteBuffer, index: Int, value: Byte) {
        // TODO
    }
}