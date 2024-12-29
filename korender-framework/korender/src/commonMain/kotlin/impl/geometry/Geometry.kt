package com.zakgof.korender.impl.geometry

import com.zakgof.korender.AttributeType
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.SCALE
import com.zakgof.korender.Attributes.SCREEN
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.IndexType
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.impl.buffer.vec3
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

fun IndexType.size() = when (this) {
    IndexType.Byte -> 1
    IndexType.Short -> 2
    IndexType.Int -> 4
}

fun AttributeType.size() = when (this) {
    AttributeType.Byte -> 1
    AttributeType.Short -> 2
    AttributeType.Int -> 4
    AttributeType.Float -> 4
}

internal object Geometry {

    suspend fun create(declaration: MeshDeclaration, appResourceLoader: ResourceLoader): Mesh =
        when (declaration) {
            is InstancedMesh ->
                builder(declaration.mesh, appResourceLoader).buildInstanced(declaration.count)

            is InstancedBillboard ->
                billboard().buildInstanced(declaration.count)

            else ->
                builder(declaration, appResourceLoader).build()
        }

    private suspend fun builder(
        declaration: MeshDeclaration,
        appResourceLoader: ResourceLoader
    ): MeshBuilder =
        when (declaration) {
            is Sphere -> sphere(declaration.radius)
            is Cube -> cube(declaration.halfSide)
            is ObjMesh -> obj(declaration.objFile, appResourceLoader)
            is Billboard -> billboard()
            is ImageQuad -> imageQuad()
            is ScreenQuad -> screenQuad()
            is CustomMesh -> create(
                declaration.id.toString(),
                declaration.vertexCount,
                declaration.indexCount,
                attrs = declaration.attributes.toTypedArray(),
                declaration.indexType
            ) {
                apply(declaration.block)
            }

            is HeightField -> heightMap(
                declaration.cellsX,
                declaration.cellsZ,
                declaration.cellWidth,
                declaration.height
            )

            else -> throw KorenderException("Unknown mesh declaration")
        }

    fun create(
        name: String,
        vertexNumber: Int,
        indexNumber: Int,
        vararg attrs: MeshAttribute,
        indexType: IndexType? = null,
        block: MeshBuilder.() -> Unit
    ) =
        MeshBuilder(
            name,
            vertexNumber,
            indexNumber,
            attrs.toList(),
            indexType
        ).apply(block)


    private fun convertIndexType(indexType: IndexType?, indexNumber: Int): IndexType {
        if (indexType == null) {
            if (indexNumber < 127)
                return IndexType.Byte
            if (indexNumber < 32767)
                return IndexType.Short
            return IndexType.Int
        }
        return indexType
    }

    internal class MeshBuilder(
        val name: String,
        val vertexNumber: Int,
        val indexNumber: Int,
        val attrs: List<MeshAttribute>,
        val attributeBuffers: List<NativeByteBuffer>,
        indexType: IndexType?
    ) : MeshInitializer {

        val realIndexType: IndexType = convertIndexType(indexType, indexNumber)
        val indexBuffer: NativeByteBuffer = NativeByteBuffer(indexNumber * realIndexType.size())
        val attrMap = attrs.indices.associate { attrs[it] to attributeBuffers[it] }

        constructor(
            name: String,
            vertexNumber: Int,
            indexNumber: Int,
            attrs: List<MeshAttribute>,
            indexType: IndexType?
        ) : this(
            name,
            vertexNumber,
            indexNumber,
            attrs,
            attrs.map { NativeByteBuffer(vertexNumber * it.primitiveType.size() * it.structSize) },
            indexType
        )

        override fun attr(attr: MeshAttribute, vararg v: Float): MeshInitializer {
            v.forEach { attrMap[attr]!!.put(it) }
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
                when (realIndexType) {
                    IndexType.Byte -> indexBuffer.put(value.toByte())
                    IndexType.Short -> indexBuffer.put(value.toShort())
                    IndexType.Int -> indexBuffer.put(value)
                }
            }
            return this
        }

        override fun indexBytes(rawBytes: ByteArray): MeshInitializer {
            indexBuffer.put(rawBytes)
            return this
        }

        override fun attrBytes(attr: MeshAttribute, rawBytes: ByteArray): MeshInitializer {
            attrMap[attr]!!.put(rawBytes)
            return this
        }

        private fun indexGet(index: Int): Int =
            when (realIndexType) {
                IndexType.Byte -> indexBuffer.byte(index).toInt()
                IndexType.Short -> indexBuffer.short(index).toInt()
                IndexType.Int -> indexBuffer.int(index)
                else -> -1
            }

        fun build(isDynamic: Boolean = false): DefaultMesh =
            DefaultMesh(name, this, isDynamic)

        fun buildInstanced(count: Int): MultiMesh =
            MultiMesh(name, instancing(count), this)

        private fun instancing(instances: Int): MeshBuilder {
            val prototype = this
            return create(
                name,
                vertexNumber * instances,
                indexNumber * instances,
                attrs = attrs.toTypedArray()
            ) {
                for (i in 0 until instances) {
                    prototype.attributeBuffers.forEachIndexed { index, prototypeAttrBuffer ->
                        attributeBuffers[index].rewind().put(prototypeAttrBuffer.rewind())
                    }
                    for (ind in 0 until prototype.indexNumber) {
                        index(prototype.indexGet(ind) + i * prototype.vertexNumber)
                    }
                }
            }
        }
    }

    open class DefaultMesh(
        name: String,
        val data: MeshBuilder,
        isDynamic: Boolean = false
    ) : Mesh {

        final override val gpuMesh: GlGpuMesh =
            GlGpuMesh(name, data.attrs, isDynamic, data.realIndexType)

        final override val modelBoundingBox: BoundingBox?
        override fun close() = gpuMesh.close()

        init {
            updateGpu()
            modelBoundingBox = if (data.attrs.contains(POS)) BoundingBox(positions()) else null
        }

        private fun positions(): List<Vec3> {
            val posBuffer = data.attrMap[POS]!!
            return (0 until data.vertexNumber).map { posBuffer.vec3(it) }
        }

        private fun updateGpu() {
            gpuMesh.update(
                data.attributeBuffers.onEach { it.rewind() },
                data.indexBuffer.rewind(),
                data.vertexNumber,
                data.indexNumber
            )
        }

        fun updateMesh(block: MeshInitializer.() -> Unit) {
            data.attributeBuffers.forEach { it.rewind() }
            data.indexBuffer.rewind()
            data.apply(block)
            updateGpu()
        }

    }

    internal class MultiMesh(
        name: String,
        data: MeshBuilder,
        private val prototype: MeshBuilder,
    ) :
        DefaultMesh(name, data, true) {

        private var initialized = false

        fun updateInstances(instances: List<MeshInstance>) {
            val protoPosBuffer = prototype.attrMap[POS]!!
            val dataPosBuffer = data.attrMap[POS]!!
            dataPosBuffer.rewind()
            instances.indices.map {
                val instance = instances[it]
                for (v in 0 until prototype.vertexNumber) {
                    val protoPos = protoPosBuffer.vec3(v)
                    val newPos = instance.transform.mat4.project(protoPos)
                    dataPosBuffer.put(newPos)
                    // TODO: normal
                }
            }
            gpuMesh.update(
                data.attributeBuffers.onEach { it.rewind() },
                data.indexBuffer.rewind(),
                prototype.vertexNumber * instances.size,
                prototype.indexNumber * instances.size
            )
            initialized = true
        }

        fun updateBillboardInstances(instances: List<BillboardInstance>) {
            val dataPosBuffer = data.attrMap[POS]!!
            val dataScaleBuffer = data.attrMap[SCALE]!!
            val dataPhiBuffer = data.attrMap[PHI]!!
            val dataTexBuffer = data.attrMap[TEX]!!
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
            gpuMesh.update(
                data.attributeBuffers.onEach { it.rewind() },
                data.indexBuffer.rewind(),
                instances.size * 4,
                instances.size * 6
            )
            initialized = true
        }

        fun updateFont(
            text: String,
            height: Float,
            aspect: Float,
            x: Float,
            y: Float,
            widths: FloatArray
        ) {
            val dataTexBuffer = data.attrMap[TEX]!!
            val dataScreenBuffer = data.attrMap[SCREEN]!!
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
            gpuMesh.update(
                data.attributeBuffers.onEach { it.rewind() },
                data.indexBuffer.rewind(),
                text.length * 4,
                text.length * 6
            )
            initialized = true
        }

        fun isInitialized() = initialized
    }

    fun screenQuad() =
        create("screen-quad", 4, 6, TEX) {
            tex(0f, 0f)
            tex(0f, 1f)
            tex(1f, 1f)
            tex(1f, 0f)
            index(0, 2, 1, 0, 3, 2)
        }

    fun cube(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create("cube", 24, 36, POS, NORMAL, TEX) {
            pos(-halfSide, -halfSide, -halfSide).normal(-1f, 0f, 0f).tex(0f, 0f)
            pos(-halfSide, halfSide, -halfSide).normal(-1f, 0f, 0f).tex(0f, 1f)
            pos(-halfSide, halfSide, halfSide).normal(-1f, 0f, 0f).tex(1f, 1f)
            pos(-halfSide, -halfSide, halfSide).normal(-1f, 0f, 0f).tex(1f, 0f)
            pos(-halfSide, -halfSide, halfSide).normal(0f, 0f, 1f).tex(0f, 0f)
            pos(-halfSide, halfSide, halfSide).normal(0f, 0f, 1f).tex(0f, 1f)
            pos(halfSide, halfSide, halfSide).normal(0f, 0f, 1f).tex(1f, 1f)
            pos(halfSide, -halfSide, halfSide).normal(0f, 0f, 1f).tex(1f, 0f)
            pos(halfSide, -halfSide, halfSide).normal(1f, 0f, 0f).tex(0f, 0f)
            pos(halfSide, halfSide, halfSide).normal(1f, 0f, 0f).tex(0f, 1f)
            pos(halfSide, halfSide, -halfSide).normal(1f, 0f, 0f).tex(1f, 1f)
            pos(halfSide, -halfSide, -halfSide).normal(1f, 0f, 0f).tex(1f, 0f)
            pos(halfSide, -halfSide, -halfSide).normal(0f, 0f, -1f).tex(0f, 0f)
            pos(halfSide, halfSide, -halfSide).normal(0f, 0f, -1f).tex(0f, 1f)
            pos(-halfSide, halfSide, -halfSide).normal(0f, 0f, -1f).tex(1f, 1f)
            pos(-halfSide, -halfSide, -halfSide).normal(0f, 0f, -1f).tex(1f, 0f)
            pos(-halfSide, halfSide, halfSide).normal(0f, 1f, 0f).tex(0f, 0f)
            pos(-halfSide, halfSide, -halfSide).normal(0f, 1f, 0f).tex(0f, 1f)
            pos(halfSide, halfSide, -halfSide).normal(0f, 1f, 0f).tex(1f, 1f)
            pos(halfSide, halfSide, halfSide).normal(0f, 1f, 0f).tex(1f, 0f)
            pos(halfSide, -halfSide, halfSide).normal(0f, -1f, 0f).tex(0f, 0f)
            pos(halfSide, -halfSide, -halfSide).normal(0f, -1f, 0f).tex(0f, 1f)
            pos(-halfSide, -halfSide, -halfSide).normal(0f, -1f, 0f).tex(1f, 1f)
            pos(-halfSide, -halfSide, halfSide).normal(0f, -1f, 0f).tex(1f, 0f)

            index(0, 2, 1, 0, 3, 2)
            index(4, 6, 5, 4, 7, 6)
            index(8, 10, 9, 8, 11, 10)
            index(12, 14, 13, 12, 15, 14)
            index(16, 18, 17, 16, 19, 18)
            index(20, 22, 21, 20, 23, 22)
            block(this)
        }

    private suspend fun obj(objFile: String, appResourceLoader: ResourceLoader): MeshBuilder {
        val model: ObjModel = ObjLoader.load(objFile, appResourceLoader)
        return create(objFile, model.vertices.size, model.indices.size, POS, NORMAL, TEX) {
            model.vertices.forEach {
                pos(it.pos).normal(it.normal).tex(it.tex.x, it.tex.y)
            }
            model.indices.forEach {
                index(it)
            }
        }
    }

    fun sphere(
        radius: Float = 1.0f,
        slices: Int = 32,
        sectors: Int = 32,
        block: MeshBuilder.() -> Unit = {}
    ) =
        create(
            "sphere",
            2 + (slices - 1) * sectors,
            sectors * 3 * 2 + (slices - 2) * sectors * 6,
            POS,
            NORMAL,
            TEX
        ) {
            pos(0f, -radius, 0f).normal(0f, -1f, 0f).tex(0f, 0f)
            for (slice in 1..<slices) {
                for (sector in 0..<sectors) {
                    val theta = PI - PI * slice / slices
                    val phi = PI * 2f * sector / sectors
                    val normal = Vec3(sin(theta) * cos(phi), cos(theta), sin(theta) * sin(phi))
                    pos(normal * radius)
                        .normal(normal)
                        .tex(sector.toFloat() / sectors, slice.toFloat() / slices)

                }
            }
            pos(0f, radius, 0f).normal(0f, 1f, 0f).tex(0f, 1f)

            for (sector in 0 until sectors) {
                index(0, sector + 1, ((sector + 1) % sectors) + 1)
            }
            for (slice in 1..<slices - 1) {
                val b = 1 + (slice - 1) * sectors
                for (sector in 0..<sectors) {
                    val nextSector = (sector + 1) % sectors
                    index(b + sector, b + sector + sectors, b + nextSector + sectors)
                    index(b + nextSector + sectors, b + nextSector, b + sector)
                }
            }
            val b = 1 + sectors * (slices - 2)
            val top = b + sectors
            for (sector in 0..<sectors) {
                index(b + sector, top, b + ((sector + 1) % sectors))
            }
            block(this)
        }


    fun heightMap(xsize: Int, zsize: Int, cell: Float, height: (Int, Int) -> Float) =
        create("heightmap", (xsize + 1) * (zsize + 1), xsize * zsize * 6, POS, NORMAL, TEX) {
            for (x in 0..xsize) {
                for (z in 0..zsize) {
                    pos(
                        x * cell - 0.5f * cell * xsize,
                        height(x, z),
                        z * cell - 0.5f * cell * zsize
                    )
                    val n = normal(x, z, xsize, zsize, cell, height)
                    normal(n)
                    tex(x.toFloat() / xsize, z.toFloat() / zsize)
                }
            }
            for (x in 0..<xsize) {
                for (z in 0..<zsize) {
                    val b = x + (xsize + 1) * z
                    index(b, b + 1, b + xsize + 1, b + xsize + 1, b + 1, b + xsize + 2)
                }
            }
        }

    private fun normal(
        x: Int,
        y: Int,
        xsize: Int,
        ysize: Int,
        cell: Float,
        height: (Int, Int) -> Float
    ): Vec3 {
        val h = height(x, y)
        val dhx = (max(x - 1, 0)..min(x + 1, xsize) step 2)
            .map { (height(it, y) - h) * (it - x) }
            .average().toFloat()
        val dhy = (max(y - 1, 0)..min(y + 1, ysize) step 2)
            .map { (height(x, it) - h) * (it - y) }
            .average().toFloat()
        return Vec3(-dhx, cell, -dhy).normalize()
    }

    fun billboard(
        position: Vec3 = Vec3.ZERO,
        scaleX: Float = 1.0f,
        scaleY: Float = 1.0f,
        phi: Float = 0.0f
    ) =
        create("billboard", 4, 6, POS, TEX, SCALE, PHI) {
            pos(position)
            tex(0f, 0f).scale(scaleX, scaleY).phi(phi)
            pos(position)
            tex(0f, 1f).scale(scaleX, scaleY).phi(phi)
            pos(position)
            tex(1f, 1f).scale(scaleX, scaleY).phi(phi)
            pos(position)
            tex(1f, 0f).scale(scaleX, scaleY).phi(phi)
            index(0, 2, 1, 0, 3, 2)
        }

    fun imageQuad() =
        create("image-quad", 4, 6, TEX) {
            tex(0f, 0f)
            tex(0f, 1f)
            tex(1f, 1f)
            tex(1f, 0f)
            index(0, 2, 1, 0, 3, 2)
        }

    fun font(reservedLength: Int): MultiMesh =
        create("font", 4, 6, TEX, SCREEN) {
            index(0, 2, 1, 0, 3, 2)
        }.buildInstanced(reservedLength)
}




