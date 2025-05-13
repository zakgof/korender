package com.zakgof.korender.impl.geometry

import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.SCALE
import com.zakgof.korender.Attributes.SCREEN
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import kotlin.math.cos
import kotlin.math.sin

internal class MeshLink(val cpuMesh: NewMesh, dynamic: Boolean) : AutoCloseable {

    val gpuMesh: GlGpuMesh = GlGpuMesh(cpuMesh.attributes, dynamic, cpuMesh.actualIndexType)

    override fun close() = gpuMesh.close()

    fun updateGpu(vertexCount: Int = cpuMesh.vertexCount, indexCount: Int = cpuMesh.indexCount) {
        gpuMesh.update(
            cpuMesh.attributeBuffers.onEach { it.rewind() },
            cpuMesh.indexBuffer?.rewind(),
            vertexCount,
            indexCount
        )
    }

    init {
        updateGpu()
    }
}

internal object NewGeometry {

    suspend fun create(meshDeclaration: MeshDeclaration, appResourceLoader: ResourceLoader): MeshLink {
        val dynamic = ((meshDeclaration as? InstancedMesh)?.static == false) or
                (meshDeclaration is InstancedBillboard)
        val cpuMesh = createCpuMesh(meshDeclaration, appResourceLoader)
        return MeshLink(cpuMesh, dynamic)
    }

    fun font(reservedLength: Int): MeshLink {
        val prototype = NewMesh(4, 6, TEX, SCREEN) {
            index(0, 2, 1, 0, 3, 2)
        }
        return MeshLink(NewInstancedMesh(prototype, reservedLength), true)
    }

    private suspend fun createCpuMesh(meshDeclaration: MeshDeclaration, appResourceLoader: ResourceLoader): NewMesh =
        when (meshDeclaration) {
            is Sphere -> sphere(meshDeclaration.radius)
            is Cube -> cube(meshDeclaration.halfSide)
            is ScreenQuad -> screenQuad()
            is Billboard -> billboard()
            is ImageQuad -> imageQuad()
            is ObjMesh -> obj(meshDeclaration.objFile, appResourceLoader)
            is CustomMesh -> NewMesh(meshDeclaration.vertexCount, meshDeclaration.indexCount, attributes = meshDeclaration.attributes.toTypedArray(), meshDeclaration.indexType, meshDeclaration.block)
            is InstancedMesh -> NewInstancedMesh(createCpuMesh(meshDeclaration.mesh, appResourceLoader), meshDeclaration.count)
            else -> throw KorenderException("Unknown mesh type $meshDeclaration")
        }

    private suspend fun obj(objFile: String, appResourceLoader: ResourceLoader): NewMesh {
        val model: ObjModel = ObjLoader.load(objFile, appResourceLoader)
        return NewMesh(model.vertices.size, model.indices.size, POS, NORMAL, TEX) {
            model.vertices.forEach {
                pos(it.pos).normal(it.normal).tex(it.tex.x, it.tex.y)
            }
            model.indices.forEach {
                index(it)
            }
        }
    }

    private fun sphere(radius: Float, slices: Int = 32, sectors: Int = 32) =
        NewMesh(
            2 + (slices - 1) * sectors,
            sectors * 3 * 2 + (slices - 2) * sectors * 6,
            POS, NORMAL, TEX
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
        NewMesh(4, 6, TEX) {
            tex(0f, 0f).tex(0f, 1f)
            tex(1f, 1f).tex(1f, 0f)
            index(0, 2, 1, 0, 3, 2)
        }

    private fun cube(halfSide: Float) =
        NewMesh(24, 36, POS, NORMAL, TEX) {
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

    private fun billboard() =
        NewMesh(4, 6, POS, TEX, SCALE, PHI) {
            pos(Vec3.ZERO)
            tex(0f, 0f).scale(1f, 1f).phi(0f)
            pos(Vec3.ZERO)
            tex(0f, 1f).scale(1f, 1f).phi(0f)
            pos(Vec3.ZERO)
            tex(1f, 1f).scale(1f, 1f).phi(0f)
            pos(Vec3.ZERO)
            tex(1f, 0f).scale(1f, 1f).phi(0f)
            index(0, 2, 1, 0, 3, 2)
        }

    private fun imageQuad() =
        NewMesh(4, 6, TEX) {
            tex(0f, 0f)
            tex(0f, 1f)
            tex(1f, 1f)
            tex(1f, 0f)
            index(0, 2, 1, 0, 3, 2)
        }
}