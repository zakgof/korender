package com.zakgof.korender

import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

enum class IndexType {
    Byte,
    Short,
    Int
}

enum class AttributeType {
    Byte,
    Short,
    Int,
    SignedByte,
    SignedShort,
    SignedInt,
    Float
}

class MeshAttribute<T>(
    val name: String,
    val structSize: Int,
    val primitiveType: AttributeType,
    val location: Int,
    val bufferAccessor: BufferAccessor<T>
)

interface BufferAccessor<T> {
    fun get(buffer: NativeByteBuffer, index: Int): T
    fun put(buffer: NativeByteBuffer, index: Int, value: T)
}

object Vec2BufferAccessor : BufferAccessor<Vec2> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec2(buffer.float(index * 2), buffer.float(index * 2 + 1))

    override fun put(buffer: NativeByteBuffer, index: Int, value: Vec2) {
        // TODO
    }
}

object Vec3BufferAccessor : BufferAccessor<Vec3> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec3(buffer.float(index * 3), buffer.float(index * 3 + 1), buffer.float(index * 3 + 2))

    override fun put(buffer: NativeByteBuffer, index: Int, value: Vec3) {
        // TODO
    }
}

object Byte4BufferAccessor : BufferAccessor<ByteArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        byteArrayOf(buffer.byte(index), buffer.byte(index + 1), buffer.byte(index + 2), buffer.byte(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: ByteArray) {
        // TODO
    }
}

object Short4BufferAccessor : BufferAccessor<ShortArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        shortArrayOf(buffer.short(index), buffer.short(index + 1), buffer.short(index + 2), buffer.short(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: ShortArray) {
        // TODO
    }
}

object Int4BufferAccessor : BufferAccessor<IntArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        intArrayOf(buffer.int(index), buffer.int(index + 1), buffer.int(index + 2), buffer.int(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: IntArray) {
        // TODO
    }
}

object Float4BufferAccessor : BufferAccessor<FloatArray> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        floatArrayOf(buffer.float(index), buffer.float(index + 1), buffer.float(index + 2), buffer.float(index + 3))

    override fun put(buffer: NativeByteBuffer, index: Int, value: FloatArray) {
        // TODO
    }
}

object FloatBufferAccessor : BufferAccessor<Float> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.float(index)

    override fun put(buffer: NativeByteBuffer, index: Int, value: Float) {
        // TODO
    }
}

object ByteBufferAccessor : BufferAccessor<Byte> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        buffer.byte(index)

    override fun put(buffer: NativeByteBuffer, index: Int, value: Byte) {
        // TODO
    }
}

object Attributes {
    val POS = MeshAttribute<Vec3>("pos", 3, AttributeType.Float, 0, Vec3BufferAccessor)
    val NORMAL = MeshAttribute<Vec3>("normal", 3, AttributeType.Float, 1, Vec3BufferAccessor)
    val TEX = MeshAttribute<Vec2>("tex", 2, AttributeType.Float, 2, Vec2BufferAccessor)
    val JOINTS_BYTE = MeshAttribute<ByteArray>("joints", 4, AttributeType.Byte, 3, Byte4BufferAccessor)
    val JOINTS_SHORT = MeshAttribute<ShortArray>("joints", 4, AttributeType.Short, 3, Short4BufferAccessor)
    val JOINTS_INT = MeshAttribute<IntArray>("joints", 4, AttributeType.Int, 3, Int4BufferAccessor)
    val WEIGHTS = MeshAttribute<FloatArray>("weights", 4, AttributeType.Float, 4, Float4BufferAccessor)
    val SCREEN = MeshAttribute<Vec2>("screen", 2, AttributeType.Float, 5, Vec2BufferAccessor)
    val SCALE = MeshAttribute<Vec2>("scale", 2, AttributeType.Float, 6, Vec2BufferAccessor)
    val PHI = MeshAttribute<Float>("phi", 1, AttributeType.Float, 7, FloatBufferAccessor)
    val B1 = MeshAttribute<Byte>("b1", 1, AttributeType.SignedByte, 8, ByteBufferAccessor)
    val B2 = MeshAttribute<Byte>("b2", 1, AttributeType.SignedByte, 9, ByteBufferAccessor)
    val B3 = MeshAttribute<Byte>("b3", 1, AttributeType.SignedByte, 10, ByteBufferAccessor)
}

interface MeshDeclaration

interface MeshInitializer {
    fun attr(attr: MeshAttribute<*>, vararg v: Float): MeshInitializer
    fun attr(attr: MeshAttribute<*>, vararg b: Byte): MeshInitializer
    fun pos(vararg position: Vec3): MeshInitializer
    fun pos(vararg v: Float): MeshInitializer
    fun normal(vararg position: Vec3): MeshInitializer
    fun normal(vararg v: Float): MeshInitializer
    fun tex(vararg position: Vec2): MeshInitializer
    fun tex(vararg v: Float): MeshInitializer
    fun scale(vararg position: Vec2): MeshInitializer
    fun scale(vararg v: Float): MeshInitializer
    fun phi(vararg v: Float): MeshInitializer
    fun index(vararg indices: Int): MeshInitializer
    fun indexBytes(rawBytes: ByteArray): MeshInitializer
    fun attrBytes(attr: MeshAttribute<*>, rawBytes: ByteArray): MeshInitializer
}

interface CpuMesh {

    val vertices: Vertices
    val indices: Indices?

    interface Vertices {
        val size: Int
        operator fun get(index: Int): Vertex
    }

    interface Vertex {
        fun pos(): Vec3?
        fun normal(): Vec3?
        fun tex(): Vec2?
        fun scale(): Vec2?
        fun phi(): Float?
        fun <T> value(attribute: MeshAttribute<T>): T?
    }

    interface Indices {
        val size: Int
        operator fun get(index: Int): Int
    }
}