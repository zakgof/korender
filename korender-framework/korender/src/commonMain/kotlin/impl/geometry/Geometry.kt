package com.zakgof.korender.impl.geometry

import com.zakgof.korender.AttributeType
import com.zakgof.korender.Attributes.INSTPOS
import com.zakgof.korender.Attributes.INSTROT
import com.zakgof.korender.Attributes.INSTSCALE
import com.zakgof.korender.Attributes.INSTSCREEN
import com.zakgof.korender.Attributes.INSTTEX
import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.IndexType
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import kotlin.math.cos
import kotlin.math.sin

internal class MeshLink(val cpuMesh: CMesh, dynamic: Boolean) : AutoCloseable {

    val gpuMesh: GlGpuMesh = GlGpuMesh(cpuMesh.attributes, dynamic, cpuMesh.actualIndexType)

    override fun close() = gpuMesh.close()

    fun updateGpu(instanceCount: Int) {
        gpuMesh.update(
            cpuMesh.attributeBuffers.onEach { it.rewind() },
            cpuMesh.indexBuffer?.rewind(),
            cpuMesh.vertexCount,
            cpuMesh.indexCount,
            instanceCount
        )
    }

    init {
        updateGpu(cpuMesh.instanceCount)
    }
}

internal object Geometry {

    suspend fun create(meshDeclaration: MeshDeclaration, appResourceLoader: ResourceLoader): MeshLink {
        val dynamic = ((meshDeclaration as? InstancedMesh)?.static == false) or
                (meshDeclaration is InstancedBillboard)
        val cpuMesh = createCpuMesh(meshDeclaration, appResourceLoader)
        return MeshLink(cpuMesh, dynamic)
    }

    fun font(reservedLength: Int): MeshLink {
        val mesh = CMesh(4, 6, reservedLength, TEX, INSTTEX, INSTSCREEN) {
            tex(0f, 0f).tex(0f, 1f).tex(1f, 1f).tex(1f, 0f)
            index(0, 1, 2, 0, 2, 3)
        }
        return MeshLink(mesh, true)
    }

    suspend fun createCpuMesh(meshDeclaration: MeshDeclaration, appResourceLoader: ResourceLoader): CMesh {
        val simpleMeshDeclaration = (meshDeclaration as? InstancedMesh)?.mesh ?: (meshDeclaration as? InstancedBillboard)?.let { Billboard(it.retentionPolicy) } ?: meshDeclaration
        val count = (meshDeclaration as? InstancedMesh)?.count ?: (meshDeclaration as? InstancedBillboard)?.count ?: -1

        return when (simpleMeshDeclaration) {
            is Sphere -> sphere(simpleMeshDeclaration.radius, simpleMeshDeclaration.slices, simpleMeshDeclaration.sectors, count)
            is Cube -> cube(simpleMeshDeclaration.halfSide, count)
            is ScreenQuad -> screenQuad()
            is Billboard -> billboard(count)
            is ImageQuad -> imageQuad()
            is ObjMesh -> obj(simpleMeshDeclaration.objFile, count, appResourceLoader)
            is CustomCpuMesh -> simpleMeshDeclaration.mesh
            is CustomMesh -> CMesh(simpleMeshDeclaration.vertexCount, simpleMeshDeclaration.indexCount, count, attributes = simpleMeshDeclaration.attributes.toTypedArray(), simpleMeshDeclaration.indexType, simpleMeshDeclaration.block)
            else -> throw KorenderException("Unknown mesh type $meshDeclaration")
        }
    }

    private suspend fun obj(objFile: String, count: Int, appResourceLoader: ResourceLoader): CMesh {
        val model: ObjModel = ObjLoader.load(objFile, appResourceLoader)
        return CMesh(model.vertices.size, model.indices.size, count, POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3) {
            model.vertices.forEach {
                pos(it.pos).normal(it.normal).tex(it.tex.x, it.tex.y)
            }
            model.indices.forEach {
                index(it)
            }
        }
    }

    private fun sphere(radius: Float, slices: Int, sectors: Int, count: Int) =
        CMesh(
            2 + (slices - 1) * sectors,
            sectors * 3 * 2 + (slices - 2) * sectors * 6,
            count,
            POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3
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
        }

    private fun screenQuad() =
        CMesh(4, 6, -1, TEX) {
            tex(0f, 0f).tex(0f, 1f)
            tex(1f, 1f).tex(1f, 0f)
            index(0, 2, 1, 0, 3, 2)
        }

    private fun cube(halfSide: Float, count: Int) =
        CMesh(24, 36, count, POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3) {
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
        }

    private fun billboard(count: Int) =
        CMesh(4, 6, count, TEX, INSTPOS, INSTSCALE, INSTROT) {
            tex(0f, 0f).tex(0f, 1f).tex(1f, 1f).tex(1f, 0f)
                .index(0, 2, 1, 0, 3, 2)
        }

    private fun imageQuad() =
        CMesh(4, 6, -1, TEX) {
            tex(0f, 0f)
            tex(0f, 1f)
            tex(1f, 1f)
            tex(1f, 0f)
            index(0, 2, 1, 0, 3, 2)
        }
}

fun IndexType.size() = when (this) {
    IndexType.Byte -> 1
    IndexType.Short -> 2
    IndexType.Int -> 4
}

fun AttributeType.size() = when (this) {
    AttributeType.Byte, AttributeType.SignedByte -> 1
    AttributeType.Short, AttributeType.SignedShort -> 2
    AttributeType.Int, AttributeType.SignedInt -> 4
    AttributeType.Float -> 4
}