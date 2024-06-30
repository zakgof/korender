package com.zakgof.korender.impl.geometry

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.engine.BillboardInstance
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.glgpu.BufferUtils
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuMesh
import com.zakgof.korender.impl.resourceStream
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin
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
import de.javagl.obj.Obj
import de.javagl.obj.ObjReader
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal object Geometry {

    fun create(declaration: MeshDeclaration, gpu: Gpu): Mesh =
        when (declaration) {
            is InstancedMesh ->
                builder(declaration.mesh).buildInstanced(gpu, declaration.count)

            is InstancedBillboard ->
                billboard().buildInstanced(gpu, declaration.count)

            else ->
                builder(declaration).build(gpu)
        }

    private fun builder(declaration: MeshDeclaration): MeshBuilder =
        when (declaration) {
            is Sphere -> sphere(declaration.radius)
            is Cube -> cube(declaration.halfSide)
            is ObjMesh -> obj(declaration.objFile)
            is Billboard -> billboard()
            is ImageQuad -> imageQuad()
            is ScreenQuad -> screenQuad()
            is CustomMesh -> create(declaration.id.toString(), declaration.vertexCount, declaration.indexCount, *declaration.attributes.toTypedArray()) {
                apply (declaration.block)
            }
            is HeightField -> heightMap(declaration.cellsX, declaration.cellsX, declaration.cellWidth, declaration.height)
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
        val vertexBuffer: ByteBuffer
        var floatVertexBuffer: FloatBuffer

        val indexBuffer: ByteBuffer
        val indexIntBuffer: IntBuffer?
        val indexShortBuffer: ShortBuffer?
        private val indexConcreteBuffer: Buffer
        var isLongIndex: Boolean

        val vertexSize: Int = attrs.sumOf { it.size } * 4

        init {
            vertexBuffer = BufferUtils.createByteBuffer(vertexNumber * vertexSize)
            floatVertexBuffer = vertexBuffer.asFloatBuffer()

            isLongIndex = vertexNumber > 32767

            indexBuffer = BufferUtils.createByteBuffer(indexNumber * (if (isLongIndex) 4 else 2))

            indexShortBuffer = if (isLongIndex) null else indexBuffer.asShortBuffer()
            indexIntBuffer = if (isLongIndex) indexBuffer.asIntBuffer() else null
            indexConcreteBuffer = if (isLongIndex) indexIntBuffer!! else indexShortBuffer!!
        }

        fun vertices(vararg values: Vec3) = values.forEach { vertices(it.x, it.y, it.z) }

        override fun vertices(vararg values: Float) {
            floatVertexBuffer.put(values)
        }

        override fun indices(vararg values: Int) {
            for (value in values) {
                if (isLongIndex)
                    indexIntBuffer!!.put(value)
                else
                    indexShortBuffer!!.put(value.toShort())
            }
        }

        private fun indexGet(index: Int): Int =
            if (isLongIndex) indexIntBuffer!![index] else indexShortBuffer!![index].toInt()


        fun updateVertex(vertexIndex: Int, block: (Vertex) -> Unit) {
            val vertex = getVertex(vertexIndex)
            block(vertex)
            putVertex(vertexIndex, vertex)
        }

        fun putVertex(vertexIndex: Int, vertex: Vertex) =
            putVertex(floatVertexBuffer, vertexIndex, vertex, vertexSize, attrs)

        fun getVertex(vertexIndex: Int): Vertex =
            getVertex(floatVertexBuffer, vertexIndex, vertexSize, attrs.toList())

        fun putVertex(vertex: Vertex) {
            for (attr in attrs) {
                attr.writer(floatVertexBuffer, vertex)
            }
        }

        fun build(gpu: Gpu, isDynamic: Boolean = false): DefaultMesh {
//            if (!isDynamic && floatVertexBuffer.position() != floatVertexBuffer.limit()) {
//                throw KorenderException("Vertex buffer not full: ${floatVertexBuffer.position()}/${floatVertexBuffer.limit()}")
//            }
//            if (!isDynamic && indexConcreteBuffer.position() != indexConcreteBuffer.limit()) {
//                throw KorenderException("Index buffer not full: ${indexConcreteBuffer.position()}/${indexConcreteBuffer.limit()}")
//            }

            return DefaultMesh(name, gpu, this, isDynamic)
        }

        fun buildInstanced(gpu: Gpu, count: Int): MultiMesh =
            MultiMesh(name, gpu, instancing(count), this, count)

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
                    that.floatVertexBuffer.rewind()
                    that.indexIntBuffer?.rewind()
                    that.indexShortBuffer?.rewind()
                    for (v in 0 until that.vertexNumber) {
                        val vertex =
                            getVertex(that.floatVertexBuffer, v, vertexSize, attrs.toList())
                        transform(i * that.vertexNumber + v, vertex)
                        putVertex(
                            floatVertexBuffer,
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

    }

    open class DefaultMesh(
        name: String,
        gpu: Gpu,
        val data: MeshBuilder,
        isDynamic: Boolean = false
    ) : Mesh {

        private val floatVertexBuffer = data.vertexBuffer.asFloatBuffer()

        final override val gpuMesh: GpuMesh =
            gpu.createMesh(name, data.attrs, data.vertexSize, isDynamic, data.isLongIndex)

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

        fun updateGpu() =
            gpuMesh.update(data.vertexBuffer, data.indexBuffer, data.vertexNumber, data.indexNumber)

        fun updateMesh(block: MeshInitializer.() -> Unit) {
            data.floatVertexBuffer.rewind()
            data.indexIntBuffer?.rewind()
            data.indexShortBuffer?.rewind()
            data.apply(block)
            updateGpu()
        }

    }

    internal class MultiMesh(
        name: String,
        gpu: Gpu,
        data: MeshBuilder,
        private val prototype: MeshBuilder,
        count: Int
    ) :
        DefaultMesh(name, gpu, data, true) {
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
                data.indexBuffer,
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
                data.indexBuffer,
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
            gpuMesh.update(data.vertexBuffer, data.indexBuffer, text.length * 4, text.length * 6)
        }
    }

    fun quad(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create("quad",4, 6, POS, NORMAL, TEX) {
            vertices(-halfSide, -halfSide, 0f, 0f, 0f, 1f, 0f, 0f)
            vertices(-halfSide, halfSide, 0f, 0f, 0f, 1f, 0f, 1f)
            vertices(halfSide, halfSide, 0f, 0f, 0f, 1f, 1f, 1f)
            vertices(halfSide, -halfSide, 0f, 0f, 0f, 1f, 1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
            block.invoke(this)
        }

    fun screenQuad() =
        create("screen-quad",4, 6, TEX) {
            vertices(0f, 0f)
            vertices(0f, 1f)
            vertices(1f, 1f)
            vertices(1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
        }


    fun cube(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create("cube",24, 36, POS, NORMAL, TEX) {
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

    private fun obj(objFile: String): MeshBuilder {
        val obj = ObjReader.read(resourceStream(objFile))
        return create(objFile, obj)
    }

    private fun create(name: String, obj: Obj): MeshBuilder {

        val mapping = mutableMapOf<String, Int>()
        val vertices = mutableListOf<FloatArray>()
        val indices = mutableListOf<Int>()

        // Rewiring
        for (i in 0 until obj.numFaces) {
            val face = obj.getFace(i)
            val newFaceindices = mutableListOf<Int>()
            for (ii in 0 until face.numVertices) {
                val vertexIndex = face.getVertexIndex(ii)
                val normalIndex = face.getNormalIndex(ii)
                val texIndex = face.getTexCoordIndex(ii)
                val key = "$vertexIndex/$normalIndex/$texIndex"
                val newVertIndex = mapping[key]
                if (newVertIndex == null) {
                    val vertex = obj.getVertex(vertexIndex)
                    val normal = obj.getNormal(normalIndex)
                    val tex = obj.getTexCoord(texIndex)
                    vertices.add(
                        floatArrayOf(
                            vertex.x,
                            vertex.y,
                            vertex.z,
                            normal.x,
                            normal.y,
                            normal.z,
                            tex.x,
                            1f - tex.y
                        )
                    )
                    mapping[key] = vertices.size - 1
                    newFaceindices.add(vertices.size - 1)
                } else {
                    newFaceindices.add(newVertIndex)
                }
            }

            val v1 = vertices[newFaceindices[1]]
            val v2 = vertices[newFaceindices[2]]
            if (abs(v1[0] - v2[0]) > 1.0f)
                println("Index buffer position: ${indices.size - 1} Delta x: ${v1[0] - v2[0]} Delta y: ${v1[1] - v2[1]} Delta z: ${v1[2] - v2[2]}")

            indices.add(newFaceindices[0])
            indices.add(newFaceindices[1])
            indices.add(newFaceindices[2])
            if (newFaceindices.size == 4) {
                indices.add(newFaceindices[0])
                indices.add(newFaceindices[2])
                indices.add(newFaceindices[3])
            }
            if (newFaceindices.size != 3 && newFaceindices.size != 4) {
                throw KorenderException("Only triangles and quads supported in .obj files")
            }
        }
        return create(name, vertices.size, indices.size, POS, NORMAL, TEX) {
            vertices.forEach() { vertices(*it) }
            indices(*indices.toIntArray())
        }
    }

    fun heightMap(xsize: Int, zsize: Int, cell: Float, height: (Int, Int) -> Float) =
        create("heightmap",(xsize + 1) * (zsize + 1), xsize * zsize * 6, POS, NORMAL, TEX) {
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
        create("billboard",4, 6, POS, TEX, SCALE, PHI) {
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
        create("image-quad",4, 6, TEX) {
            vertices(0f, 0f)
            vertices(0f, 1f)
            vertices(1f, 1f)
            vertices(1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
        }

    fun font(gpu: Gpu, reservedLength: Int): MultiMesh =
        create("font",4, 6, TEX, SCREEN) {
            indices(0, 2, 1, 0, 3, 2)
        }.buildInstanced(gpu, reservedLength)
}

private fun getVertex(
    floatVertexBuffer: FloatBuffer,
    vertexIndex: Int,
    vertexSize: Int,
    attrs: List<Attribute>
): Vertex {
    val vertex = Vertex()
    floatVertexBuffer.position(vertexIndex * vertexSize / 4)
    for (attr in attrs) {
        attr.reader(floatVertexBuffer, vertex)
    }
    return vertex
}

private fun putVertex(
    floatVertexBuffer: FloatBuffer,
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



