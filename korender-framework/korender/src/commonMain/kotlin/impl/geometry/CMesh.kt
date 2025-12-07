package com.zakgof.korender.impl.geometry

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.IndexType
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal open class CMesh(
    val vertexCount: Int,
    val indexCount: Int,
    val instanceCount: Int = -1,
    attrs: List<MeshAttribute<*>>,
    indexType: IndexType? = null,
) : Mesh, MeshInitializer {

    var instancesInitialized: Boolean = false

    val attributes = attrs.filter { !it.instance || instanceCount > 0 }

    val attributeBuffers: List<NativeByteBuffer> = attributes
        .map {
            val count = if (it.instance) instanceCount else vertexCount
            NativeByteBuffer(count * it.primitiveType.size() * it.structSize)
        }
    val attrMap: Map<MeshAttribute<*>, NativeByteBuffer> = attributeBuffers.indices.associate { attributes[it] to attributeBuffers[it] }

    val actualIndexType: IndexType = convertIndexType(indexType, indexCount)
    val indexBuffer: NativeByteBuffer? = if (indexCount > 0) NativeByteBuffer(indexCount * actualIndexType.size()) else null

    override val vertices = object : AbstractList<Mesh.Vertex>() {

        override val size = vertexCount

        override fun get(index: Int): Mesh.Vertex = object : Mesh.Vertex {

            override val pos
                get() = value(POS)
            override val normal
                get() = value(NORMAL)
            override val tex
                get() = value(TEX)

            override fun <T> value(attribute: MeshAttribute<T>) =
                attrMap[attribute]?.let { attribute.bufferAccessor.get(it, index) }
        }

    }
    override val indices = if (indexCount > 0) object : AbstractList<Int>() {
        override val size = indexCount
        override fun get(index: Int): Int = when (actualIndexType) {
            IndexType.Byte -> indexBuffer!!.byte(index).toInt()
            IndexType.Short -> indexBuffer!!.short(index).toInt()
            IndexType.Int -> indexBuffer!!.int(index)
        }
    } else null

    constructor(
        vertexCount: Int,
        indexCount: Int,
        instanceCount: Int = -1,
        vararg attributes: MeshAttribute<*>,
        indexType: IndexType? = null,
        block: MeshInitializer.() -> Unit,
    ) : this(vertexCount, indexCount, instanceCount, attributes.toList(), indexType = indexType) {
        apply(block)
    }

    private fun convertIndexType(indexType: IndexType?, indexNumber: Int): IndexType =
        indexType ?: if (indexNumber < 127)
            IndexType.Byte
        else if (indexNumber < 32767)
            IndexType.Short
        else IndexType.Int

    override fun attr(attr: MeshAttribute<*>, vararg v: Float): MeshInitializer {
        v.forEach { attrMap[attr]!!.put(it) }
        return this
    }

    override fun attr(attr: MeshAttribute<*>, vararg b: Byte): MeshInitializer {
        b.forEach { attrMap[attr]!!.put(it) }
        return this
    }

    override fun <T> attrSet(attr: MeshAttribute<T>, index: Int, value: T): MeshInitializer {
        attr.bufferAccessor.put(attrMap[attr]!!, index, value)
        return this
    }

    override fun pos(vararg position: Vec3): MeshInitializer {
        position.forEach { pos(it.x, it.y, it.z) }
        return this
    }

    override fun pos(vararg v: Float): MeshInitializer = attr(POS, *v)

    override fun normal(vararg normal: Vec3): MeshInitializer {
        normal.forEach { normal(it.x, it.y, it.z) }
        return this
    }

    override fun normal(vararg v: Float): MeshInitializer = attr(NORMAL, *v)

    override fun tex(vararg tex: Vec2): MeshInitializer {
        tex.forEach { tex(it.x, it.y) }
        return this
    }

    override fun tex(vararg v: Float): MeshInitializer = attr(TEX, *v)

    override fun index(vararg indices: Int): MeshInitializer {
        for (value in indices) {
            when (actualIndexType) {
                IndexType.Byte -> indexBuffer!!.put(value.toByte())
                IndexType.Short -> indexBuffer!!.put(value.toShort())
                IndexType.Int -> indexBuffer!!.put(value)
            }
        }
        return this
    }

    override fun indexBytes(rawBytes: ByteArray): MeshInitializer {
        indexBuffer!!.put(rawBytes)
        return this
    }

    override fun attrBytes(attr: MeshAttribute<*>, rawBytes: ByteArray): MeshInitializer {
        attrMap[attr]!!.put(rawBytes)
        return this
    }

    fun updateMesh(block: MeshInitializer.() -> Unit) {
        attributeBuffers.forEach { it.rewind() }
        indexBuffer?.rewind()
        apply(block)
    }

    // TODO: Totally rework this
    fun determineVertexCount(): Int {
        val verticesEstimates = attrMap.filter { !it.key.instance }
            .map {
                it.value.position() / (it.key.structSize * it.key.primitiveType.size())
            }
        return verticesEstimates.first()
    }

    fun determineIndexCount() = (indexBuffer?.position() ?: 0) / actualIndexType.size()
}