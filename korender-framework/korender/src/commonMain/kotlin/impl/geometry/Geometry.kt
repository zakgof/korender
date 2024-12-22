package com.zakgof.korender.impl.geometry

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.buffer.BufferUtils
import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.buffer.Inter
import com.zakgof.korender.buffer.Shorter
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.impl.gpu.GpuMesh
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.Attributes.NORMAL
import com.zakgof.korender.mesh.Attributes.PHI
import com.zakgof.korender.mesh.Attributes.POS
import com.zakgof.korender.mesh.Attributes.SCALE
import com.zakgof.korender.mesh.Attributes.SCREEN
import com.zakgof.korender.mesh.Attributes.TEX
import com.zakgof.korender.mesh.Billboard
import com.zakgof.korender.mesh.Cube
import com.zakgof.korender.mesh.CustomMesh
import com.zakgof.korender.mesh.HeightField
import com.zakgof.korender.mesh.ImageQuad
import com.zakgof.korender.mesh.InstancedBillboard
import com.zakgof.korender.mesh.InstancedMesh
import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.mesh.MeshInitializer
import com.zakgof.korender.mesh.ObjMesh
import com.zakgof.korender.mesh.ScreenQuad
import com.zakgof.korender.mesh.Sphere
import com.zakgof.korender.mesh.Vertex
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

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

    private suspend fun builder(declaration: MeshDeclaration, appResourceLoader: ResourceLoader): MeshBuilder =
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
                *declaration.attributes.toTypedArray()
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
        vararg attrs: Attribute,
        block: MeshBuilder.() -> Unit
    ) =
        MeshBuilder(name, vertexNumber, indexNumber, attrs.toList()).apply(block)

    class MeshBuilder(
        val name: String,
        val vertexNumber: Int,
        val indexNumber: Int,
        val attrs: List<Attribute>
    ) : MeshInitializer {
        val vertexBuffer: Floater
        val indexInter: Inter?
        val indexShorter: Shorter?
        var isLongIndex: Boolean

        val vertexSize: Int = attrs.sumOf { it.size } * 4

        init {
            vertexBuffer = BufferUtils.createFloatBuffer(vertexNumber * vertexSize / 4) // TODO /4
            isLongIndex = vertexNumber > 32767
            indexShorter = if (isLongIndex) null else BufferUtils.createShortBuffer(indexNumber)
            indexInter = if (isLongIndex) BufferUtils.createIntBuffer(indexNumber) else null
        }

        fun vertices(vararg values: Vec3) = values.forEach { vertices(it.x, it.y, it.z) }

        override fun vertices(vararg values: Float) {
            vertexBuffer.put(values)
        }

        override fun indices(vararg values: Int) {
            for (value in values) {
                if (isLongIndex)
                    indexInter!!.put(value)
                else
                    indexShorter!!.put(value.toShort())
            }
        }

        private fun indexGet(index: Int): Int =
            if (isLongIndex) indexInter!![index] else indexShorter!![index].toInt()


        fun updateVertex(vertexIndex: Int, block: (Vertex) -> Unit) {
            val vertex = getVertex(vertexIndex)
            block(vertex)
            putVertex(vertexIndex, vertex)
        }

        fun putVertex(vertexIndex: Int, vertex: Vertex) =
            putVertex(vertexBuffer, vertexIndex, vertex, vertexSize, attrs)

        fun getVertex(vertexIndex: Int): Vertex =
            getVertex(vertexBuffer, vertexIndex, vertexSize, attrs.toList())

        fun putVertex(vertex: Vertex) {
            for (attr in attrs) {
                attr.writer(vertexBuffer, vertex)
            }
        }

        fun build(isDynamic: Boolean = false): DefaultMesh =
            DefaultMesh(name, this, isDynamic)

        fun buildInstanced(count: Int): MultiMesh =
            MultiMesh(name, instancing(count), this, count)

        fun instancing(
            instances: Int,
            transform: (Int, Vertex) -> Unit = { _, _ -> }
        ): MeshBuilder {
            val that = this
            return create(
                name,
                vertexNumber * instances,
                indexNumber * instances,
                *attrs.toTypedArray()
            ) {
                for (i in 0 until instances) {
                    that.vertexBuffer.rewind()
                    that.indexInter?.rewind()
                    that.indexShorter?.rewind()
                    for (v in 0 until that.vertexNumber) {
                        val vertex =
                            getVertex(that.vertexBuffer, v, vertexSize, attrs.toList())
                        transform(i * that.vertexNumber + v, vertex)
                        putVertex(
                            vertexBuffer,
                            i * that.vertexNumber + v,
                            vertex,
                            vertexSize,
                            attrs.toList()
                        )
                    }
                    for (ind in 0 until that.indexNumber) {
                        indices(that.indexGet(ind) + i * that.vertexNumber)
                    }
                }
            }
        }

        override fun vertex(block: Vertex.() -> Unit) = putVertex(Vertex().apply(block))

        override fun vertex(vertex: Vertex) = putVertex(vertex)

        fun indexBuffer() =
            if (isLongIndex) indexInter!! else indexShorter!!
    }

    open class DefaultMesh(
        name: String,
        val data: MeshBuilder,
        isDynamic: Boolean = false
    ) : Mesh {

        private val floatVertexBuffer = data.vertexBuffer

        final override val gpuMesh: GpuMesh =
            GlGpuMesh(name, data.attrs, data.vertexSize, isDynamic, data.isLongIndex)

        final override val modelBoundingBox: BoundingBox?
        override fun close() = gpuMesh.close()

        init {
            updateGpu()
            modelBoundingBox = if (data.attrs.contains(POS)) BoundingBox(positions()) else null
        }

        fun positions(): List<Vec3> {
            val positionOffset = data.attrs.takeWhile { it != POS }.sumOf { it.size }
            return (0 until data.vertexNumber).map {
                floatVertexBuffer.position(it * data.vertexSize / 4 + positionOffset)
                val x = floatVertexBuffer.get()
                val y = floatVertexBuffer.get()
                val z = floatVertexBuffer.get()
                Vec3(x, y, z)
            }
        }

        private fun updateGpu() {
            gpuMesh.update(
                data.vertexBuffer.apply { rewind() },
                data.indexBuffer().apply { rewind() },
                data.vertexNumber,
                data.indexNumber
            )
        }

        fun updateMesh(block: MeshInitializer.() -> Unit) {
            data.vertexBuffer.rewind()
            data.indexInter?.rewind()
            data.indexShorter?.rewind()
            data.apply(block)
            updateGpu()
        }

    }

    internal class MultiMesh(
        name: String,
        data: MeshBuilder,
        private val prototype: MeshBuilder,
        count: Int
    ) :
        DefaultMesh(name, data, true) {
        fun updateInstances(instances: List<MeshInstance>) {
            instances.indices.map {
                val instance = instances[it]
                for (v in 0..<prototype.vertexNumber) {
                    data.updateVertex(prototype.vertexNumber * it + v) {
                        it.pos = instance.transform.mat4.project(prototype.getVertex(v).pos!!)
                        // TODO: normal
                    }
                }
            }
            gpuMesh.update(
                data.vertexBuffer,
                if (data.isLongIndex) data.indexInter!! else data.indexShorter!!,
                prototype.vertexNumber * instances.size,
                prototype.indexNumber * instances.size
            )
        }

        fun updateBillboardInstances(instances: List<BillboardInstance>) {
            instances.indices.map {
                val instance = instances[it]
                data.putVertex(
                    it * 4 + 0,
                    Vertex(
                        pos = instance.pos,
                        scale = instance.scale,
                        phi = instance.phi,
                        tex = Vec2(0f, 0f)
                    )
                )
                data.putVertex(
                    it * 4 + 1,
                    Vertex(
                        pos = instance.pos,
                        scale = instance.scale,
                        phi = instance.phi,
                        tex = Vec2(0f, 1f)
                    )
                )
                data.putVertex(
                    it * 4 + 2,
                    Vertex(
                        pos = instance.pos,
                        scale = instance.scale,
                        phi = instance.phi,
                        tex = Vec2(1f, 1f)
                    )
                )
                data.putVertex(
                    it * 4 + 3,
                    Vertex(
                        pos = instance.pos,
                        scale = instance.scale,
                        phi = instance.phi,
                        tex = Vec2(1f, 0f)
                    )
                )
            }
            gpuMesh.update(
                data.vertexBuffer,
                if (data.isLongIndex) data.indexInter!! else data.indexShorter!!, // TODO method DRY
                instances.size * 4,
                instances.size * 6
            )
        }

        fun updateFont(
            text: String,
            height: Float,
            aspect: Float,
            x: Float,
            y: Float,
            widths: FloatArray
        ) {
            var xx = x
            for (i in text.indices) {
                val c = text[i].code
                val ratio = widths[c]
                val width = height * ratio * aspect
                data.updateVertex(i * 4 + 0) {
                    it.tex = Vec2((c % 16) / 16.0f, (c / 16) / 16.0f)
                    it.screen = Vec2(xx, y)
                }
                data.updateVertex(i * 4 + 1) {
                    it.tex = Vec2((c % 16 + ratio) / 16.0f, (c / 16) / 16.0f)
                    it.screen = Vec2(xx + width, y)
                }
                data.updateVertex(i * 4 + 2) {
                    it.tex = Vec2((c % 16 + ratio) / 16.0f, (c / 16 + 1f) / 16.0f)
                    it.screen = Vec2(xx + width, y - height)
                }
                data.updateVertex(i * 4 + 3) {
                    it.tex = Vec2((c % 16) / 16.0f, (c / 16 + 1f) / 16.0f)
                    it.screen = Vec2(xx, y - height)
                }
                xx += width
            }
            gpuMesh.update(
                data.vertexBuffer,
                if (data.isLongIndex) data.indexInter!! else data.indexShorter!!,
                text.length * 4,
                text.length * 6
            )
        }
    }

    fun quad(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create("quad", 4, 6, POS, NORMAL, TEX) {
            vertices(-halfSide, -halfSide, 0f, 0f, 0f, 1f, 0f, 0f)
            vertices(-halfSide, halfSide, 0f, 0f, 0f, 1f, 0f, 1f)
            vertices(halfSide, halfSide, 0f, 0f, 0f, 1f, 1f, 1f)
            vertices(halfSide, -halfSide, 0f, 0f, 0f, 1f, 1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
            block.invoke(this)
        }

    fun screenQuad() =
        create("screen-quad", 4, 6, TEX) {
            vertices(0f, 0f)
            vertices(0f, 1f)
            vertices(1f, 1f)
            vertices(1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
        }


    fun cube(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create("cube", 24, 36, POS, NORMAL, TEX) {
            vertices(-halfSide, -halfSide, -halfSide, -1f, 0f, 0f, 0f, 0f)
            vertices(-halfSide, halfSide, -halfSide, -1f, 0f, 0f, 0f, 1f)
            vertices(-halfSide, halfSide, halfSide, -1f, 0f, 0f, 1f, 1f)
            vertices(-halfSide, -halfSide, halfSide, -1f, 0f, 0f, 1f, 0f)
            vertices(-halfSide, -halfSide, halfSide, 0f, 0f, 1f, 0f, 0f)
            vertices(-halfSide, halfSide, halfSide, 0f, 0f, 1f, 0f, 1f)
            vertices(halfSide, halfSide, halfSide, 0f, 0f, 1f, 1f, 1f)
            vertices(halfSide, -halfSide, halfSide, 0f, 0f, 1f, 1f, 0f)
            vertices(halfSide, -halfSide, halfSide, 1f, 0f, 0f, 0f, 0f)
            vertices(halfSide, halfSide, halfSide, 1f, 0f, 0f, 0f, 1f)
            vertices(halfSide, halfSide, -halfSide, 1f, 0f, 0f, 1f, 1f)
            vertices(halfSide, -halfSide, -halfSide, 1f, 0f, 0f, 1f, 0f)
            vertices(halfSide, -halfSide, -halfSide, 0f, 0f, -1f, 0f, 0f)
            vertices(halfSide, halfSide, -halfSide, 0f, 0f, -1f, 0f, 1f)
            vertices(-halfSide, halfSide, -halfSide, 0f, 0f, -1f, 1f, 1f)
            vertices(-halfSide, -halfSide, -halfSide, 0f, 0f, -1f, 1f, 0f)
            vertices(-halfSide, halfSide, halfSide, 0f, 1f, 0f, 0f, 0f)
            vertices(-halfSide, halfSide, -halfSide, 0f, 1f, 0f, 0f, 1f)
            vertices(halfSide, halfSide, -halfSide, 0f, 1f, 0f, 1f, 1f)
            vertices(halfSide, halfSide, halfSide, 0f, 1f, 0f, 1f, 0f)
            vertices(halfSide, -halfSide, halfSide, 0f, -1f, 0f, 0f, 0f)
            vertices(halfSide, -halfSide, -halfSide, 0f, -1f, 0f, 0f, 1f)
            vertices(-halfSide, -halfSide, -halfSide, 0f, -1f, 0f, 1f, 1f)
            vertices(-halfSide, -halfSide, halfSide, 0f, -1f, 0f, 1f, 0f)

            indices(0, 2, 1, 0, 3, 2)
            indices(4, 6, 5, 4, 7, 6)
            indices(8, 10, 9, 8, 11, 10)
            indices(12, 14, 13, 12, 15, 14)
            indices(16, 18, 17, 16, 19, 18)
            indices(20, 22, 21, 20, 23, 22)
            block(this)
        }

    private suspend fun obj(objFile: String, appResourceLoader: ResourceLoader): MeshBuilder {
        val model: ObjModel = ObjLoader.load(objFile, appResourceLoader)
        return create(objFile, model.vertices.size, model.indices.size, POS, NORMAL, TEX) {
            model.vertices.forEach {
                vertices(it.pos, it.normal)
                vertices(it.tex.x, it.tex.y)
            }
            model.indices.forEach {
                indices(it)
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
            vertices(0f, -radius, 0f, 0f, -1f, 0f, 0f, 0f)
            for (slice in 1..<slices) {
                for (sector in 0..<sectors) {
                    val theta = PI - PI * slice / slices
                    val phi = PI * 2f * sector / sectors
                    val normal = Vec3(sin(theta) * cos(phi), cos(theta), sin(theta) * sin(phi))
                    vertices(
                        radius * normal.x, radius * normal.y, radius * normal.z,
                        normal.x, normal.y, normal.z,
                        sector.toFloat() / sectors, slice.toFloat() / slices,
                    )
                }
            }
            vertices(0f, radius, 0f, 0f, 1f, 0f, 0f, 1f)

            for (sector in 0 until sectors) {
                indices(0, sector + 1, ((sector + 1) % sectors) + 1)
            }
            for (slice in 1..<slices - 1) {
                val b = 1 + (slice - 1) * sectors
                for (sector in 0..<sectors) {
                    val nextSector = (sector + 1) % sectors
                    indices(b + sector, b + sector + sectors, b + nextSector + sectors)
                    indices(b + nextSector + sectors, b + nextSector, b + sector)
                }
            }
            val b = 1 + sectors * (slices - 2)
            val top = b + sectors
            for (sector in 0..<sectors) {
                indices(b + sector, top, b + ((sector + 1) % sectors))
            }
            block(this)
        }


    fun heightMap(xsize: Int, zsize: Int, cell: Float, height: (Int, Int) -> Float) =
        create("heightmap", (xsize + 1) * (zsize + 1), xsize * zsize * 6, POS, NORMAL, TEX) {
            for (x in 0..xsize) {
                for (z in 0..zsize) {
                    vertices(
                        x * cell - 0.5f * cell * xsize,
                        height(x, z),
                        z * cell - 0.5f * cell * zsize
                    )
                    val n = normal(x, z, xsize, zsize, cell, height)
                    vertices(n)
                    vertices(x.toFloat() / xsize, z.toFloat() / zsize)
                }
            }
            for (x in 0..<xsize) {
                for (z in 0..<zsize) {
                    val b = x + (xsize + 1) * z
                    indices(b, b + 1, b + xsize + 1, b + xsize + 1, b + 1, b + xsize + 2)
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
            vertices(position)
            vertices(0f, 0f, scaleX, scaleY, phi)
            vertices(position)
            vertices(0f, 1f, scaleX, scaleY, phi)
            vertices(position)
            vertices(1f, 1f, scaleX, scaleY, phi)
            vertices(position)
            vertices(1f, 0f, scaleX, scaleY, phi)
            indices(0, 2, 1, 0, 3, 2)
        }

    fun imageQuad() =
        create("image-quad", 4, 6, TEX) {
            vertices(0f, 0f)
            vertices(0f, 1f)
            vertices(1f, 1f)
            vertices(1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
        }

    fun font(reservedLength: Int): MultiMesh =
        create("font", 4, 6, TEX, SCREEN) {
            indices(0, 2, 1, 0, 3, 2)
        }.buildInstanced(reservedLength)
}

private fun getVertex(
    vertexBuffer: Floater,
    vertexIndex: Int,
    vertexSize: Int,
    attrs: List<Attribute>
): Vertex {
    val vertex = Vertex()
    vertexBuffer.position(vertexIndex * vertexSize / 4)
    for (attr in attrs) {
        attr.reader(vertexBuffer, vertex)
    }
    return vertex
}

private fun putVertex(
    floatVertexBuffer: Floater,
    vertexIndex: Int,
    vertex: Vertex,
    vertexSize: Int,
    attrs: List<Attribute>
) {
    floatVertexBuffer.position(vertexIndex * vertexSize / 4)
    for (attr in attrs) {
        attr.writer(floatVertexBuffer, vertex)
    }
}



