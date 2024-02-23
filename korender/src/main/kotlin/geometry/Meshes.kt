package com.zakgof.korender.geometry

import com.zakgof.korender.KorenderException
import com.zakgof.korender.geometry.Attributes.NORMAL
import com.zakgof.korender.geometry.Attributes.PHI
import com.zakgof.korender.geometry.Attributes.POS
import com.zakgof.korender.geometry.Attributes.SCALE
import com.zakgof.korender.geometry.Attributes.TEX
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.math.BoundingBox
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import de.javagl.obj.Obj
import java.nio.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Meshes {

    fun create(vertexNumber: Int, indexNumber: Int, vararg attrs: Attribute, block: MeshBuilder.() -> Unit) =
        MeshBuilder(vertexNumber, indexNumber, attrs).apply(block)

    class MeshBuilder(
        private val vertexNumber: Int,
        private val indexNumber: Int,
        private val attrs: Array<out Attribute>
    ) {
        private val vertexBuffer: ByteBuffer
        private var floatVertexBuffer: FloatBuffer

        private val indexBuffer: ByteBuffer
        private val indexIntBuffer: IntBuffer?
        private val indexShortBuffer: ShortBuffer?
        private val indexConcreteBuffer: Buffer
        private var isLongIndex: Boolean

        private val vertexSize: Int = attrs.sumOf { it.size } * 4

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

        fun vertices(vararg values: Float) = floatVertexBuffer.put(values)

        fun indices(vararg values: Int) {
            for (value in values) {
                if (isLongIndex)
                    indexIntBuffer!!.put(value)
                else
                    indexShortBuffer!!.put(value.toShort())
            }
        }

        private fun indexGet(): Int = if (isLongIndex) indexIntBuffer!!.get() else indexShortBuffer!!.get().toInt()

        fun transformPos(function: (Vec3) -> Vec3): MeshBuilder {
            val positionOffset = attrs.takeWhile { it != POS }.sumOf { it.size }
            for (v in 0 until vertexNumber) {
                floatVertexBuffer.position(v * vertexSize / 4 + positionOffset)
                val x = floatVertexBuffer.get()
                val y = floatVertexBuffer.get()
                val z = floatVertexBuffer.get()
                floatVertexBuffer.position(v * vertexSize / 4 + positionOffset)
                vertices(function.invoke(Vec3(x, y, z)))
            }
            return this
        }

        fun transformPos(transform: Transform) = transformPos { transform.mat4().project(it) }

        fun build(gpu: Gpu, isDynamic: Boolean = false): DefaultMesh {
            if (!isDynamic && floatVertexBuffer.position() != floatVertexBuffer.limit()) {
                throw KorenderException("Vertex buffer not full: ${floatVertexBuffer.position()}/${floatVertexBuffer.limit()}")
            }
            if (!isDynamic && indexConcreteBuffer.position() != indexConcreteBuffer.limit()) {
                throw KorenderException("Index buffer not full: ${indexConcreteBuffer.position()}/${indexConcreteBuffer.limit()}")
            }

            return DefaultMesh(
                gpu,
                vertexBuffer.rewind(),
                indexBuffer.rewind(),
                vertexNumber,
                indexNumber,
                attrs.toList(),
                vertexSize,
                isDynamic
            )
        }

        fun instancing(instances: Int, transform: (Int, Vertex) -> Unit = { _, _ -> }): MeshBuilder {
            val that = this
            return create(vertexNumber * instances, indexNumber * instances, *attrs) {
                for (i in 0 until instances) {
                    that.floatVertexBuffer.rewind()
                    that.indexIntBuffer?.rewind()
                    that.indexShortBuffer?.rewind()
                    for (v in 0 until that.vertexNumber) {
                        val vertex = getVertex(that.floatVertexBuffer, v, vertexSize, attrs.toList())
                        transform(i * that.vertexNumber + v, vertex)
                        putVertex(floatVertexBuffer, i * that.vertexNumber + v, vertex, vertexSize, attrs.toList())
                    }
                    for (ind in 0 until that.indexNumber) {
                        indices(that.indexGet() + i * that.vertexNumber)
                    }
                }
            }
        }
    }

    class DefaultMesh(
        gpu: Gpu,
        private val vb: ByteBuffer,
        private val ib: ByteBuffer,
        var vertices: Int,
        var indices: Int,
        private val attrs: List<Attribute>,
        private val vertexSize: Int,
        isDynamic: Boolean = false
    ) : Mesh {

        private val floatVertexBuffer = vb.asFloatBuffer()

        override val gpuMesh: GpuMesh = gpu.createMesh(attrs, vertexSize, isDynamic)

        override val modelBoundingBox: BoundingBox?

        init {
            gpuMesh.update(vb, ib, vertices, indices)
            modelBoundingBox = if (attrs.contains(POS)) BoundingBox(positions()) else null
        }

        fun positions(): List<Vec3> {
            val positionOffset = attrs.takeWhile { it != POS }.sumOf { it.size }
            return (0 until vertices).map {
                floatVertexBuffer.position(it * vertexSize / 4 + positionOffset)
                val x = floatVertexBuffer.get()
                val y = floatVertexBuffer.get()
                val z = floatVertexBuffer.get()
                Vec3(x, y, z)
            }
        }

        fun updateGpu() = gpuMesh.update(vb, ib, vertices, indices)

        fun updateVertex(vertexIndex: Int, block: (Vertex) -> Unit) {
            val vertex = getVertex(vertexIndex)
            block(vertex)
            putVertex(vertexIndex, vertex)
        }

        fun putVertex(vertexIndex: Int, vertex: Vertex) =
            putVertex(floatVertexBuffer, vertexIndex, vertex, vertexSize, attrs)

        fun getVertex(vertexIndex: Int): Vertex = getVertex(floatVertexBuffer, vertexIndex, vertexSize, attrs.toList())
    }

    fun quad(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create(4, 6, POS, NORMAL, TEX) {
            vertices(-halfSide, -halfSide, 0f, 0f, 0f, 1f, 0f, 0f)
            vertices(-halfSide, halfSide, 0f, 0f, 0f, 1f, 0f, 1f)
            vertices(halfSide, halfSide, 0f, 0f, 0f, 1f, 1f, 1f)
            vertices(halfSide, -halfSide, 0f, 0f, 0f, 1f, 1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
            block.invoke(this)
        }

    fun screenQuad() =
        create(4, 6, TEX) {
            vertices(0f, 0f)
            vertices(0f, 1f)
            vertices(1f, 1f)
            vertices(1f, 0f)
            indices(0, 2, 1, 0, 3, 2)
        }


    fun cube(halfSide: Float = 0.5f, block: MeshBuilder.() -> Unit = {}) =
        create(24, 36, POS, NORMAL, TEX) {
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

    fun sphere(radius: Float = 1.0f, slices: Int = 32, sectors: Int = 32, block: MeshBuilder.() -> Unit = {}) =
        create(2 + (slices - 1) * sectors, sectors * 3 * 2 + (slices - 2) * sectors * 6, POS, NORMAL, TEX) {
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

    fun create(obj: Obj): MeshBuilder {

        val mapping = mutableMapOf<String, Int>()
        val vertices = mutableListOf<FloatArray>()
        val indices = mutableListOf<Int>()

        // Rewiring
        for (i in 0 until obj.numFaces) {
            val face = obj.getFace(i)
            val newFaceIndices = mutableListOf<Int>()
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
                    newFaceIndices.add(vertices.size - 1)
                } else {
                    newFaceIndices.add(newVertIndex)
                }
            }

            val v1 = vertices[newFaceIndices[1]]
            val v2 = vertices[newFaceIndices[2]]
            if (abs(v1[0] - v2[0]) > 1.0f)
                println("Index buffer position: ${indices.size - 1} Delta x: ${v1[0] - v2[0]} Delta y: ${v1[1] - v2[1]} Delta z: ${v1[2] - v2[2]}")

            indices.add(newFaceIndices[0])
            indices.add(newFaceIndices[1])
            indices.add(newFaceIndices[2])
            if (newFaceIndices.size == 4) {
                indices.add(newFaceIndices[0])
                indices.add(newFaceIndices[2])
                indices.add(newFaceIndices[3])
            }
            if (newFaceIndices.size != 3 && newFaceIndices.size != 4) {
                throw KorenderException("Only triangles and quads supported in .obj files")
            }
        }
        return create(vertices.size, indices.size, POS, NORMAL, TEX) {
            vertices.forEach() { vertices(*it) }
            indices(*indices.toIntArray())
        }
    }

    fun heightMap(xsize: Int, ysize: Int, cell: Float, height: (Int, Int) -> Float) =
        create((xsize + 1) * (ysize + 1), xsize * ysize * 6, POS, NORMAL, TEX) {
            for (x in 0..xsize) {
                for (y in 0..ysize) {
                    vertices(x * cell - 0.5f * cell * xsize, height(x, y), y * cell - 0.5f * cell * ysize)
                    val n = normal(x, y, xsize, ysize, cell, height)
                    vertices(n)
                    vertices(x.toFloat() / (xsize + 1), y.toFloat() / (ysize + 1))
                }
            }
            for (x in 0..<xsize) {
                for (y in 0..<ysize) {
                    val b = x + (xsize + 1) * y
                    indices(b, b + 1, b + xsize + 1, b + xsize + 1, b + 1, b + xsize + 2)
                }
            }
        }

    private fun normal(x: Int, y: Int, xsize: Int, ysize: Int, cell: Float, height: (Int, Int) -> Float): Vec3 {
        val h = height(x, y)
        val dhx = (max(x - 1, 0)..min(x + 1, xsize) step 2)
            .map { (height(it, y) - h) * (it - x) }
            .average().toFloat()
        val dhy = (max(y - 1, 0)..min(y + 1, ysize) step 2)
            .map { (height(x, it) - h) * (it - y) }
            .average().toFloat()
        return Vec3(-dhx, cell, -dhy).normalize()
    }

    fun billboard(position: Vec3 = Vec3.ZERO, scaleX: Float = 1.0f, scaleY: Float = 1.0f, phi: Float = 0.0f) =
        create(4, 6, POS, TEX, SCALE, PHI) {
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



