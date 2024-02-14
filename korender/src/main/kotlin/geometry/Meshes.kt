package com.zakgof.korender.geometry

import com.zakgof.korender.Gpu
import com.zakgof.korender.KorenderException
import com.zakgof.korender.geometry.Attributes.NORMAL
import com.zakgof.korender.geometry.Attributes.POS
import com.zakgof.korender.geometry.Attributes.TEX
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Vec3
import de.javagl.obj.Obj
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import kotlin.math.abs

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
        private var isLongIndex: Boolean

        private val vertexSize: Int

        init {
            this.vertexSize = attrs.sumOf { it.size } * 4
            this.vertexBuffer = BufferUtils.createByteBuffer(vertexNumber * vertexSize)
            this.floatVertexBuffer = vertexBuffer.asFloatBuffer()

            this.isLongIndex = vertexNumber > 32767

            this.indexBuffer = BufferUtils.createByteBuffer(indexNumber * (if (isLongIndex) 4 else 2))

            this.indexShortBuffer = if (isLongIndex) null else indexBuffer.asShortBuffer()
            this.indexIntBuffer = if (isLongIndex) indexBuffer.asIntBuffer() else null
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

        fun positions(): List<Vec3> {
            val positionOffset = attrs.takeWhile { it != POS }.sumOf { it.size }
            return (0 until vertexNumber).map {
                floatVertexBuffer.position(it * vertexSize / 4 + positionOffset)
                val x = floatVertexBuffer.get()
                val y = floatVertexBuffer.get()
                val z = floatVertexBuffer.get()
                Vec3(x, y, z)
            }
        }

        fun build(gpu: Gpu): GpuMesh {
            // TODO: validate vb/ib full
            return gpu.createMesh(
                vertexBuffer.rewind(),
                indexBuffer.rewind(),
                vertexNumber,
                indexNumber,
                attrs.toList(),
                vertexSize,
                false
            )
        }


    }

    fun quad(halfSide: Float): MeshBuilder =
        create(4, 6, POS, NORMAL, TEX) {
            vertices(-halfSide, -halfSide, 0f, 0f, 0f, 1f, 0f, 0f)
            vertices(-halfSide, halfSide, 0f, 0f, 0f, 1f, 0f, 1f)
            vertices(halfSide, halfSide, 0f, 0f, 0f, 1f, 1f, 1f)
            vertices(halfSide, -halfSide, 0f, 0f, 0f, 1f, 1f, 0f)
            indices(0, 1, 2, 0, 2, 3)
        }

    fun cube(hs: Float = 0.5f): MeshBuilder =
        create(24, 36, POS, NORMAL, TEX) {
            vertices(-hs, -hs, -hs, -1f, 0f, 0f, 0f, 0f)
            vertices(-hs, hs, -hs, -1f, 0f, 0f, 0f, 1f)
            vertices(-hs, hs, hs, -1f, 0f, 0f, 1f, 1f)
            vertices(-hs, -hs, hs, -1f, 0f, 0f, 1f, 0f)
            vertices(-hs, -hs, hs, 0f, 0f, 1f, 0f, 0f)
            vertices(-hs, hs, hs, 0f, 0f, 1f, 0f, 1f)
            vertices(hs, hs, hs, 0f, 0f, 1f, 1f, 1f)
            vertices(hs, -hs, hs, 0f, 0f, 1f, 1f, 0f)
            vertices(hs, -hs, hs, 1f, 0f, 0f, 0f, 0f)
            vertices(hs, hs, hs, 1f, 0f, 0f, 0f, 1f)
            vertices(hs, hs, -hs, 1f, 0f, 0f, 1f, 1f)
            vertices(hs, -hs, -hs, 1f, 0f, 0f, 1f, 0f)
            vertices(hs, -hs, -hs, 0f, 0f, -1f, 0f, 0f)
            vertices(hs, hs, -hs, 0f, 0f, -1f, 0f, 1f)
            vertices(-hs, hs, -hs, 0f, 0f, -1f, 1f, 1f)
            vertices(-hs, -hs, -hs, 0f, 0f, -1f, 1f, 0f)
            vertices(-hs, hs, hs, 0f, 1f, 0f, 0f, 0f)
            vertices(-hs, hs, -hs, 0f, 1f, 0f, 0f, 1f)
            vertices(hs, hs, -hs, 0f, 1f, 0f, 1f, 1f)
            vertices(hs, hs, hs, 0f, 1f, 0f, 1f, 0f)
            vertices(hs, -hs, hs, 0f, -1f, 0f, 0f, 0f)
            vertices(hs, -hs, -hs, 0f, -1f, 0f, 0f, 1f)
            vertices(-hs, -hs, -hs, 0f, -1f, 0f, 1f, 1f)
            vertices(-hs, -hs, hs, 0f, -1f, 0f, 1f, 0f)

            indices(0, 2, 1, 0, 3, 2)
            indices(4, 6, 5, 4, 7, 6)
            indices(8, 10, 9, 8, 11, 10)
            indices(12, 14, 13, 12, 15, 14)
            indices(16, 18, 17, 16, 19, 18)
            indices(20, 22, 21, 20, 23, 22)
        }

    fun sphere(radius: Float, slices: Int = 32, sectors: Int = 32) =
        create(2 + (slices - 1) * sectors, sectors * 3 * 2 + (slices - 2) * sectors * 6, POS, NORMAL, TEX) {
            vertices(0f, -radius, 0f, 0f, -1f, 0f, 0f, 0f)
            for (slice in 1..slices - 1) {
                for (sector in 0..sectors - 1) {
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
            for (slice in 1..slices - 2) {
                val b = 1 + (slice - 1) * sectors
                for (sector in 0..sectors - 1) {
                    val nextSector = (sector + 1) % sectors
                    indices(b + sector, b + sector + sectors, b + nextSector + sectors)
                    indices(b + nextSector + sectors, b + nextSector, b + sector)
                }
            }
            val b = 1 + sectors * (slices - 2)
            val top = b + sectors
            for (sector in 0..sectors - 1) {
                indices(b + sector, top, b + ((sector + 1) % sectors))
            }
        }

    fun create(obj: Obj): MeshBuilder {

        val mapping = mutableMapOf<String, Int>()
        val vertices = mutableListOf<FloatArray>()
        var indices = mutableListOf<Int>()

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
                    vertices.add(floatArrayOf(vertex.x, vertex.y, vertex.z, normal.x, normal.y, normal.z, tex.x, 1f - tex.y))
                    mapping[key] = vertices.size - 1
                    newFaceIndices.add(vertices.size - 1)
                } else {
                    newFaceIndices.add(newVertIndex)
                }
            }

            val v1 = vertices[newFaceIndices[1]]
            val v2 = vertices[newFaceIndices[2]]
            if (abs(v1[0]-v2[0]) > 1.0f)
                println("Index buffer position: ${indices.size-1} Delta x: ${v1[0]-v2[0]} Delta y: ${v1[1]-v2[1]} Delta z: ${v1[2]-v2[2]}")

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
}

