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
    fun put(buffer: NativeByteBuffer, index: Int, value: T) {
        seek(buffer, index)
        put(buffer, value)
    }
    fun seek(buffer: NativeByteBuffer, index: Int)
    fun put(buffer: NativeByteBuffer, value: T)
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

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(8 * index)

    override fun put(buffer: NativeByteBuffer, value: Vec2) {
        buffer.put(value.x)
        buffer.put(value.y)
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
}

internal object Byte4BufferAccessor : BufferAccessor<ByteArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        byteArrayOf(buffer.byte(index), buffer.byte(index + 1), buffer.byte(index + 2), buffer.byte(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(4 * index)

    override fun put(buffer: NativeByteBuffer, value: ByteArray) {
        // TODO
    }
}

internal object Short4BufferAccessor : BufferAccessor<ShortArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        shortArrayOf(buffer.short(index), buffer.short(index + 1), buffer.short(index + 2), buffer.short(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(8 * index)

    override fun put(buffer: NativeByteBuffer, value: ShortArray) {
        // TODO
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
}

internal object Float4BufferAccessor : BufferAccessor<FloatArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        floatArrayOf(buffer.float(index), buffer.float(index + 1), buffer.float(index + 2), buffer.float(index + 3))

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(16 * index)

    override fun put(buffer: NativeByteBuffer, value: FloatArray) {
        value.forEach { buffer.put(it) }
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
}

object ByteBufferAccessor : BufferAccessor<Byte> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.byte(index)

    override fun seek(buffer: NativeByteBuffer, index: Int) =
        buffer.position(index)

    override fun put(buffer: NativeByteBuffer, value: Byte) {
        buffer.put(value)
    }
}