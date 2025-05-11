package com.zakgof.korender.impl.geometry

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.SCALE
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.CpuMesh
import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.geometry.Geometry.convertIndexType
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class NewMesh(
    vertexCount: Int,
    indexCount: Int,
    vararg attributes: MeshAttribute<Any>
) : CpuMesh {

    private val attributeBuffers: List<NativeByteBuffer> = attributes.map { NativeByteBuffer(vertexCount * it.primitiveType.size() * it.structSize) }
    private val attrMap: Map<MeshAttribute<*>, NativeByteBuffer> = attributes.indices.associate { attributes[it] to attributeBuffers[it] }

    val realIndexType: IndexType = convertIndexType(indexType, indexNumber)
    val indexBuffer: NativeByteBuffer? = if (indexCount > 0) NativeByteBuffer(indexCount * realIndexType.size()) else null

    override val vertices: CpuMesh.Vertices = object : CpuMesh.Vertices {

        override val size = vertexCount

        override fun get(index: Int): CpuMesh.Vertex = object : CpuMesh.Vertex {

            override fun pos() = value(POS)
            override fun normal() = value(NORMAL)
            override fun tex() = value(TEX)
            override fun scale() = value(SCALE)
            override fun phi() = value(PHI)

            override fun <T> value(attribute: MeshAttribute<T>) =
                attrMap[attribute]?.let { attribute.bufferAccessor.get(it, index) }
        }

    }
    override val indices: CpuMesh.Indices? = if (indexCount > 0) object : CpuMesh.Indices {
        override val size = indexCount
        override fun get(index: Int): Int =
    } else null

    init {

    }

}