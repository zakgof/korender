package com.zakgof.korender.impl.geometry

import com.zakgof.korender.IndexType
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.geometry.MeshAttributes.COLORTEXINDEX
import com.zakgof.korender.impl.geometry.MeshAttributes.NORMAL
import com.zakgof.korender.impl.geometry.MeshAttributes.POS
import com.zakgof.korender.impl.geometry.MeshAttributes.TEX
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal open class CMesh(
    val vertexCount: Int,
    val indexCount: Int,
    val instanceCount: Int = -1,
    attrs: List<InternalMeshAttribute<*>>,
    indexType: IndexType? = null,
) : Mesh, MeshInitializer {

    var instancesInitialized: Boolean = false

    val attributes = attrs.filter { !it.instance || instanceCount > 0 }

    val attributeBuffers: List<NativeByteBuffer> = attributes
        .map {
            val count = if (it.instance) instanceCount else vertexCount
            NativeByteBuffer(count * it.primitiveType.size() * it.structSize)
        }
    val attrMap: Map<InternalMeshAttribute<*>, NativeByteBuffer> =
        attributeBuffers.indices.associate { attributes[it] to attributeBuffers[it] }

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
                attrMap[attribute as InternalMeshAttribute<T>]?.let { attribute.bufferAccessor.get(it, index) }
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
    ) : this(vertexCount, indexCount, instanceCount, attributes.map { it as InternalMeshAttribute }.toList(), indexType = indexType) {
        apply(block)
    }

    private fun convertIndexType(indexType: IndexType?, indexNumber: Int): IndexType =
        indexType ?: if (indexNumber < 127)
            IndexType.Byte
        else if (indexNumber < 32767)
            IndexType.Short
        else IndexType.Int

    override fun <T> attr(attr: MeshAttribute<T>, vararg value: T): MeshInitializer {
        value.forEach {
            (attr as InternalMeshAttribute<T>).bufferAccessor.put(attrMap[attr]!!, it)
        }
        return this
    }

    override fun <T> attrSet(attr: MeshAttribute<T>, index: Int, value: T): MeshInitializer {
        (attr as InternalMeshAttribute<T>).bufferAccessor.put(attrMap[attr]!!, index, value)
        return this
    }

    override fun embed(prototype: Mesh, transform: Transform, colorTexIndex: Int?) {
        val targetAttrs = attributes.filter { !it.instance }
        val commonAttrs = targetAttrs
            .filter { hasPrototypeAttribute(prototype, it) }
            .filterNot { colorTexIndex != null && it == COLORTEXINDEX }

        prototype.vertices.forEach { vertex ->
            commonAttrs.forEach { attr ->
                copy(attr, vertex, transform)
            }
        }

        if (colorTexIndex != null && attrMap.containsKey(COLORTEXINDEX)) {
            repeat(prototype.vertices.size) {
                attr(COLORTEXINDEX, colorTexIndex.toByte())
            }
        }

        val offset = attrMap[POS]
            ?.position()
            ?.div(POS.structSize * POS.primitiveType.size())
            ?.minus(prototype.vertices.size)
            ?: 0
        prototype.indices?.let {
            index(*it.map { i -> i + offset }.toIntArray())
        }
    }

    private fun hasPrototypeAttribute(prototype: Mesh, attr: InternalMeshAttribute<*>): Boolean {
        if (prototype is CMesh) {
            return prototype.attributes.contains(attr)
        }
        val firstVertex = prototype.vertices.firstOrNull() ?: return false
        @Suppress("UNCHECKED_CAST")
        return firstVertex.value(attr as MeshAttribute<Any?>) != null
    }

    @Suppress("UNCHECKED_CAST")
    private fun copy(attr: InternalMeshAttribute<*>, vertex: Mesh.Vertex, transform: Transform) {
        when (attr) {
            POS -> vertex.pos?.let { attr(POS, transform * it) }
            NORMAL -> vertex.normal?.let { attr(NORMAL, transform.applyToDirection(it).normalize()) }
            else -> {
                val value = vertex.value(attr as MeshAttribute<Any?>) ?: return
                attr(attr, value)
            }
        }
    }

    override fun pos(vararg position: Vec3): MeshInitializer {
        attr(POS, *position)
        return this
    }

    override fun normal(vararg normal: Vec3): MeshInitializer {
        attr(NORMAL, *normal)
        return this
    }

    override fun tex(vararg tex: Vec2): MeshInitializer {
        attr(TEX, *tex)
        return this
    }

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

    override fun <T> attrBytes(attr: MeshAttribute<T>, rawBytes: ByteArray): MeshInitializer {
        attrMap[attr as InternalMeshAttribute]!!.put(rawBytes)
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
