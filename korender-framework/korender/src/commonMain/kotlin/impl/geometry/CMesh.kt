package com.zakgof.korender.impl.geometry

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.SCALE
import com.zakgof.korender.Attributes.SCREEN
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Mesh
import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.impl.buffer.vec3
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal open class CMesh(
    val vertexCount: Int,
    val indexCount: Int,
    val attributes: List<MeshAttribute<*>>,
    indexType: IndexType? = null
) : Mesh, MeshInitializer {

    val attributeBuffers: List<NativeByteBuffer> = attributes.map { NativeByteBuffer(vertexCount * it.primitiveType.size() * it.structSize) }
    val attrMap: Map<MeshAttribute<*>, NativeByteBuffer> = attributes.indices.associate { attributes[it] to attributeBuffers[it] }

    val actualIndexType: IndexType = convertIndexType(indexType, indexCount)
    val indexBuffer: NativeByteBuffer? = if (indexCount > 0) NativeByteBuffer(indexCount * actualIndexType.size()) else null

    override val vertices: Mesh.Vertices = object : Mesh.Vertices {

        override val size = vertexCount

        override fun get(index: Int): Mesh.Vertex = object : Mesh.Vertex {

            override fun pos() = value(POS)
            override fun normal() = value(NORMAL)
            override fun tex() = value(TEX)
            override fun scale() = value(SCALE)
            override fun phi() = value(PHI)

            override fun <T> value(attribute: MeshAttribute<T>) =
                attrMap[attribute]?.let { attribute.bufferAccessor.get(it, index) }
        }

    }
    override val indices: Mesh.Indices? = if (indexCount > 0) object : Mesh.Indices {
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
        vararg attributes: MeshAttribute<*>,
        indexType: IndexType? = null,
        block: MeshInitializer.() -> Unit
    ) : this(vertexCount, indexCount, attributes.toList(), indexType = indexType) {
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

    override fun scale(vararg scale: Vec2): MeshInitializer {
        scale.forEach { tex(it.x, it.y) }
        return this
    }

    override fun scale(vararg v: Float): MeshInitializer = attr(SCALE, *v)

    override fun phi(vararg v: Float): MeshInitializer = attr(PHI, *v)

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
}

internal class MultiMesh(val prototype: CMesh, instances: Int) :
    CMesh(prototype.vertexCount * instances, prototype.indexCount * instances, prototype.attributes) {

    var initialized = false

    init {
        attributeBuffers.forEach { it.rewind() }
        for (i in 0 until instances) {
            prototype.attributeBuffers.forEachIndexed { index, prototypeAttrBuffer ->
                attributeBuffers[index].put(prototypeAttrBuffer.rewind())
            }
            for (ind in 0 until prototype.indexCount) {
                index(prototype.indices!![ind] + i * prototype.vertexCount)
            }
        }
    }

    fun updateFont(
        text: String,
        height: Float,
        aspect: Float,
        x: Float,
        y: Float,
        widths: FloatArray
    ) {
        val dataTexBuffer = attrMap[TEX]!!
        val dataScreenBuffer = attrMap[SCREEN]!!
        dataTexBuffer.rewind()
        dataScreenBuffer.rewind()
        var xx = x
        for (i in text.indices) {
            val c = text[i].code
            val ratio = widths[c]
            val width = height * ratio * aspect
            dataTexBuffer.put((c % 16) / 16.0f)
            dataTexBuffer.put((c / 16) / 16.0f)
            dataScreenBuffer.put(xx)
            dataScreenBuffer.put(y)

            dataTexBuffer.put((c % 16 + ratio) / 16.0f)
            dataTexBuffer.put((c / 16) / 16.0f)
            dataScreenBuffer.put(xx + width)
            dataScreenBuffer.put(y)

            dataTexBuffer.put((c % 16 + ratio) / 16.0f)
            dataTexBuffer.put((c / 16 + 1f) / 16.0f)
            dataScreenBuffer.put(xx + width)
            dataScreenBuffer.put(y - height)

            dataTexBuffer.put((c % 16) / 16.0f)
            dataTexBuffer.put((c / 16 + 1f) / 16.0f)
            dataScreenBuffer.put(xx)
            dataScreenBuffer.put(y - height)

            xx += width
        }
        initialized = true
    }

    fun updateInstances(instances: List<MeshInstance>) {
        val protoPosBuffer = prototype.attrMap[POS]!!
        val dataPosBuffer = attrMap[POS]!!
        val protoNormalBuffer = prototype.attrMap[NORMAL]
        val dataNormalBuffer = attrMap[NORMAL]
        dataPosBuffer.rewind()
        dataNormalBuffer?.rewind()
        instances.indices.map {
            val instance = instances[it]
            val normalMatrix = instance.transform.mat4.invTranspose()
            for (v in 0 until prototype.vertexCount) {
                val newPos = instance.transform.mat4.project(protoPosBuffer.vec3(v))
                dataPosBuffer.put(newPos)
                if (protoNormalBuffer != null) {
                    val newNormal = normalMatrix * protoNormalBuffer.vec3(v)
                    dataNormalBuffer!!.put(newNormal)
                }
            }
        }
        initialized = true
    }

    fun updateBillboardInstances(instances: List<BillboardInstance>) {
        val dataPosBuffer = attrMap[POS]!!
        val dataScaleBuffer = attrMap[SCALE]!!
        val dataPhiBuffer = attrMap[PHI]!!
        val dataTexBuffer = attrMap[TEX]!!
        dataPosBuffer.rewind()
        dataScaleBuffer.rewind()
        dataPhiBuffer.rewind()
        dataTexBuffer.rewind()
        instances.indices.map {
            val instance = instances[it]
            dataPosBuffer.put(instance.pos)
            dataScaleBuffer.put(instance.scale.x)
            dataScaleBuffer.put(instance.scale.y)
            dataPhiBuffer.put(instance.phi)
            dataTexBuffer.put(0f)
            dataTexBuffer.put(0f)
            dataPosBuffer.put(instance.pos)
            dataScaleBuffer.put(instance.scale.x)
            dataScaleBuffer.put(instance.scale.y)
            dataPhiBuffer.put(instance.phi)
            dataTexBuffer.put(0f)
            dataTexBuffer.put(1f)
            dataPosBuffer.put(instance.pos)
            dataScaleBuffer.put(instance.scale.x)
            dataScaleBuffer.put(instance.scale.y)
            dataPhiBuffer.put(instance.phi)
            dataTexBuffer.put(1f)
            dataTexBuffer.put(1f)
            dataPosBuffer.put(instance.pos)
            dataScaleBuffer.put(instance.scale.x)
            dataScaleBuffer.put(instance.scale.y)
            dataPhiBuffer.put(instance.phi)
            dataTexBuffer.put(1f)
            dataTexBuffer.put(0f)
        }
        initialized = true
    }
}