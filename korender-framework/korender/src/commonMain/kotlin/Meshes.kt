package com.zakgof.korender

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
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
    val bufferAccessor: BufferAccessor<T>,
    val instance: Boolean = false
)

interface BufferAccessor<T> {
    fun get(buffer: NativeByteBuffer, index: Int): T
    fun put(buffer: NativeByteBuffer, index: Int, value: T)
}

object Vec2BufferAccessor : BufferAccessor<Vec2> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec2(buffer.float(index * 2), buffer.float(index * 2 + 1))

    override fun put(buffer: NativeByteBuffer, index: Int, value: Vec2) {
        buffer.position(8 * index)
        buffer.put(value.x)
        buffer.put(value.y)
    }
}

object Vec3BufferAccessor : BufferAccessor<Vec3> {
    override fun get(buffer: NativeByteBuffer, index: Int) =
        Vec3(buffer.float(index * 3), buffer.float(index * 3 + 1), buffer.float(index * 3 + 2))

    override fun put(buffer: NativeByteBuffer, index: Int, value: Vec3) {
        buffer.position(12 * index)
        buffer.put(value.x)
        buffer.put(value.y)
        buffer.put(value.z)
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
        buffer.position(4 * 4 * index)
        value.forEach { buffer.put(it) }
    }
}

object FloatBufferAccessor : BufferAccessor<Float> {
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

object Attributes {
    val POS = MeshAttribute("pos", 3, AttributeType.Float, 0, Vec3BufferAccessor)
    val NORMAL = MeshAttribute("normal", 3, AttributeType.Float, 1, Vec3BufferAccessor)
    val TEX = MeshAttribute("tex", 2, AttributeType.Float, 2, Vec2BufferAccessor)
    val JOINTS_BYTE = MeshAttribute("joints", 4, AttributeType.Byte, 3, Byte4BufferAccessor)
    val JOINTS_SHORT = MeshAttribute("joints", 4, AttributeType.Short, 3, Short4BufferAccessor)
    val JOINTS_INT = MeshAttribute("joints", 4, AttributeType.Int, 3, Int4BufferAccessor)
    val WEIGHTS = MeshAttribute("weights", 4, AttributeType.Float, 4, Float4BufferAccessor)
    val SCALE = MeshAttribute("scale", 2, AttributeType.Float, 5, Vec2BufferAccessor)
    val B1 = MeshAttribute("b1", 1, AttributeType.SignedByte, 8, ByteBufferAccessor)
    val B2 = MeshAttribute("b2", 1, AttributeType.SignedByte, 9, ByteBufferAccessor)
    val B3 = MeshAttribute("b3", 1, AttributeType.SignedByte, 10, ByteBufferAccessor)
    val MODEL0 = MeshAttribute("instanceModel0", 4, AttributeType.Float, 11, Float4BufferAccessor, true)
    val MODEL1 = MeshAttribute("instanceModel1", 4, AttributeType.Float, 12, Float4BufferAccessor, true)
    val MODEL2 = MeshAttribute("instanceModel2", 4, AttributeType.Float, 13, Float4BufferAccessor, true)
    val MODEL3 = MeshAttribute("instanceModel3", 4, AttributeType.Float, 14, Float4BufferAccessor, true)
    val INSTPOS = MeshAttribute("instpos", 3, AttributeType.Float, 11, Vec3BufferAccessor, true)
    val INSTSCALE = MeshAttribute("instscale", 2, AttributeType.Float, 12, Vec2BufferAccessor, true)
    val INSTROT = MeshAttribute("instrot", 1, AttributeType.Float, 13, FloatBufferAccessor, true)
    val INSTTEX = MeshAttribute("insttexrect", 4, AttributeType.Float, 11, Float4BufferAccessor, true)
    val INSTSCREEN = MeshAttribute("instscreentect", 4, AttributeType.Float, 12, Float4BufferAccessor, true)
}

interface MeshDeclaration

interface MeshInitializer {
    fun attr(attr: MeshAttribute<*>, vararg v: Float): MeshInitializer
    fun attr(attr: MeshAttribute<*>, vararg b: Byte): MeshInitializer
    fun pos(vararg position: Vec3): MeshInitializer
    fun pos(vararg v: Float): MeshInitializer
    fun normal(vararg normal: Vec3): MeshInitializer
    fun normal(vararg v: Float): MeshInitializer
    fun tex(vararg tex: Vec2): MeshInitializer
    fun tex(vararg v: Float): MeshInitializer
    fun index(vararg indices: Int): MeshInitializer
    fun indexBytes(rawBytes: ByteArray): MeshInitializer
    fun attrBytes(attr: MeshAttribute<*>, rawBytes: ByteArray): MeshInitializer
    fun <T> attrSet(attr: MeshAttribute<T>, index: Int, value: T): MeshInitializer
}

interface Mesh {

    val vertices: Vertices
    val indices: Indices?

    interface Vertices {
        val size: Int
        operator fun get(index: Int): Vertex
    }

    interface Vertex {
        val pos: Vec3?
        val normal: Vec3?
        val tex: Vec2?
        fun <T> value(attribute: MeshAttribute<T>): T?
    }

    interface Indices {
        val size: Int
        operator fun get(index: Int): Int
    }
}

class MutableMesh : Mesh {

    override val vertices = MutableVertices()
    override val indices = MutableIndices()

    class MutableVertices : Mesh.Vertices {
        private val list = mutableListOf<Mesh.Vertex>()
        override val size
            get() = list.size

        override fun get(index: Int) = list[index]
        operator fun plusAssign(vertex: Mesh.Vertex) {
            list += vertex
        }
    }

    class MutableVertex : Mesh.Vertex {

        override var pos: Vec3? = null
        override var normal: Vec3? = null
        override var tex: Vec2? = null

        fun pos(p: Vec3) = apply { pos = p }
        fun normal(n: Vec3) = apply { normal = n }
        fun tex(t: Vec2) = apply { tex = t }

        @Suppress("UNCHECKED_CAST")
        override fun <T> value(attribute: MeshAttribute<T>): T? = when (attribute) {
            POS -> pos
            NORMAL -> normal
            TEX -> tex
            else -> null
        } as T?
    }

    class MutableIndices : Mesh.Indices {
        private val list = mutableListOf<Int>()
        override val size
            get() = list.size

        override fun get(index: Int) = list[index]

        fun index(vararg i: Int) = apply { list += i.toList() }
    }

}