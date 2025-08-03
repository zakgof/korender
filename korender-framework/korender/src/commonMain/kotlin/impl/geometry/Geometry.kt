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
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

internal class MeshLink(val cpuMesh: CMesh, dynamic: Boolean) : AutoCloseable {

    val gpuMesh: GlGpuMesh = GlGpuMesh(cpuMesh.attributes, dynamic, cpuMesh.actualIndexType)

    override fun close() = gpuMesh.close()

    fun updateGpu(instanceCount: Int, instanceDataOnly: Boolean) {
        gpuMesh.update(
            cpuMesh.attributeBuffers.onEach { it.rewind() },
            cpuMesh.indexBuffer?.rewind(),
            cpuMesh.vertexCount,
            cpuMesh.indexCount,
            instanceCount,
            instanceDataOnly
        )
    }

    init {
        updateGpu(cpuMesh.instanceCount, false)
    }
}

internal object Geometry {

    fun create(meshDeclaration: MeshDeclaration, loader: Loader): MeshLink? {
        val dynamic = ((meshDeclaration as? InstancedMesh)?.static == false) or
                (meshDeclaration is InstancedBillboard)
        return createCpuMesh(meshDeclaration, loader)?.let {
            MeshLink(it, dynamic)
        }
    }

    fun createCpuMesh(meshDeclaration: MeshDeclaration, loader: Loader): CMesh? {
        val simpleMeshDeclaration = (meshDeclaration as? InstancedMesh)?.mesh ?: (meshDeclaration as? InstancedBillboard)?.let { Billboard(it.retentionPolicy) } ?: meshDeclaration
        val count = (meshDeclaration as? Instanceable)?.count ?: -1

        return when (simpleMeshDeclaration) {
            is Sphere -> sphere(simpleMeshDeclaration.radius, simpleMeshDeclaration.slices, simpleMeshDeclaration.sectors, count)
            is Cube -> cube(simpleMeshDeclaration.halfSide, count)
            is DecalCube -> decalCube(simpleMeshDeclaration.halfSide, count)
            is ScreenQuad -> screenQuad()
            is Billboard -> billboard(count)
            is ImageQuad -> imageQuad()
            is Quad -> quad(simpleMeshDeclaration.halfSideX, simpleMeshDeclaration.halfSideY, count)
            is Disk -> disk(simpleMeshDeclaration.radius, simpleMeshDeclaration.sectors, count)
            is CylinderSide -> cylinderSide(simpleMeshDeclaration.radius, simpleMeshDeclaration.height, simpleMeshDeclaration.sectors, count)
            is ConeTop -> coneTop(simpleMeshDeclaration.radius, simpleMeshDeclaration.height, simpleMeshDeclaration.sectors, count)
            is HeightField -> heightField(simpleMeshDeclaration.cellsX, simpleMeshDeclaration.cellsZ, simpleMeshDeclaration.cellWidth, simpleMeshDeclaration.height, count)
            is ObjMesh -> loader.safeBytes(simpleMeshDeclaration.objFile) { obj(it, count) }
            is CustomCpuMesh -> toCMesh(simpleMeshDeclaration.mesh, count)
            is CustomMesh -> CMesh(simpleMeshDeclaration.vertexCount, simpleMeshDeclaration.indexCount, count, attributes = simpleMeshDeclaration.attributes.toTypedArray(), simpleMeshDeclaration.indexType, simpleMeshDeclaration.block)
            is FontMesh -> font(count)
            else -> throw KorenderException("Unknown mesh type $meshDeclaration")
        }
    }

    private fun quad(halfSideX: Float, halfSideY: Float, count: Int) = CMesh(
        4,
        6,
        count,
        POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3
    ) {
        pos(Vec3(-halfSideX, -halfSideY, 0f)).tex(0f, 0f).normal(1.z)
        pos(Vec3(halfSideX, -halfSideY, 0f)).tex(1f, 0f).normal(1.z)
        pos(Vec3(halfSideX, halfSideY, 0f)).tex(1f, 1f).normal(1.z)
        pos(Vec3(-halfSideX, halfSideY, 0f)).tex(0f, 1f).normal(1.z)
        index(0, 1, 2, 0, 2, 3)
    }

    private fun disk(radius: Float, sectors: Int, count: Int) = CMesh(
        sectors * 2,
        sectors * 3,
        count,
        POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3
    ) {
        for (sector in 0 until sectors) {
            val phi = PI * 2f * sector / sectors
            pos(Vec3(radius * cos(phi), radius * sin(phi), 0f))
                .normal(1.z)
                .tex(0.5f + 0.5f * cos(phi), 0.5f + 0.5f * sin(phi))
        }
        pos(Vec3.ZERO)
            .normal(1.z)
            .tex(0.5f, 0.5f)
        for (sector in 0 until sectors) {
            index(sector, (sector + 1) % sectors, sectors)
        }
    }

    private fun coneTop(radius: Float, height: Float, sectors: Int, count: Int) = CMesh(
        sectors * 2,
        sectors * 3,
        count,
        POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3
    ) {
        val base = 1f / Vec2(radius, height).length()
        val xSlope = height * base
        val ySlope = radius * base
        for (sector in 0 until sectors) {
            val phi = PI * 2f * sector / sectors
            val normal = Vec3(xSlope * cos(phi), ySlope, xSlope * sin(phi))
            pos(Vec3(radius * cos(phi), 0f, radius * sin(phi)))
                .normal(normal)
                .tex(sector.toFloat() / sectors, 0f)
            pos(height.y)
                .normal(normal)
                .tex(sector.toFloat() / sectors, 1f)
        }
        val d = 2 * sectors
        for (sector in 0 until sectors) {
            index(sector * 2, (sector * 2 + 1) % d, (sector * 2 + 2) % d)
        }
    }

    private fun cylinderSide(radius: Float, height: Float, sectors: Int, count: Int) = CMesh(
        sectors * 2,
        sectors * 6,
        count,
        POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3
    ) {
        for (sector in 0 until sectors) {
            val phi = PI * 2f * sector / sectors
            val cosPhi = cos(phi)
            val sinPhi = sin(phi)
            val normal = Vec3(cosPhi, 0f, sinPhi)
            pos(Vec3(radius * cosPhi, 0f, radius * sinPhi))
                .normal(normal)
                .tex(sector.toFloat() / sectors, 0f)
            pos(Vec3(radius * cosPhi, height, radius * sinPhi))
                .normal(normal)
                .tex(sector.toFloat() / sectors, 1f)
        }
        val d = 2 * sectors
        for (sector in 0 until sectors) {
            index(sector * 2, (sector * 2 + 1) % d, (sector * 2 + 2) % d)
            index((sector * 2 + 1) % d, (sector * 2 + 3) % d, (sector * 2 + 2) % d)
        }
    }

    private fun heightField(xsize: Int, zsize: Int, cell: Float, height: (Int, Int) -> Float, count: Int) = CMesh(
        (xsize + 1) * (zsize + 1),
        xsize * zsize * 6,
        count,
        POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3
    ) {
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

    private fun obj(objFileBytes: ByteArray, count: Int): CMesh {
        val model: ObjModel = ObjLoader.load(objFileBytes)
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

    private fun decalCube(halfSide: Float, count: Int) =
        CMesh(8, 36, count, POS, MODEL0, MODEL1, MODEL2, MODEL3) {
            pos(-halfSide, -halfSide, -halfSide)
            pos(halfSide, -halfSide, -halfSide)
            pos(-halfSide, halfSide, -halfSide)
            pos(halfSide, halfSide, -halfSide)
            pos(-halfSide, -halfSide, halfSide)
            pos(halfSide, -halfSide, halfSide)
            pos(-halfSide, halfSide, halfSide)
            pos(halfSide, halfSide, halfSide)

            index(0, 1, 2, 1, 3, 2)
            index(5, 4, 7, 4, 6, 7)
            index(4, 0, 6, 0, 2, 6)
            index(1, 5, 3, 5, 7, 3)
            index(2, 3, 6, 3, 7, 6)
            index(4, 5, 0, 5, 1, 0)
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

    private fun font(count: Int) =
        CMesh(4, 6, count, TEX, INSTTEX, INSTSCREEN) {
            tex(0f, 0f).tex(0f, 1f).tex(1f, 1f).tex(1f, 0f)
            index(0, 1, 2, 0, 2, 3)
        }

    private fun toCMesh(mesh: Mesh, count: Int): CMesh? {
        if (mesh is CMesh) {
            return mesh
        }
        return CMesh(mesh.vertices.size, mesh.indices?.size ?: -1, count, POS, NORMAL, TEX) {
            (0 until mesh.vertices.size).forEach {
                val vertex = mesh.vertices[it]
                pos(vertex.pos!!).normal(vertex.normal!!).tex(vertex.tex!!)
            }
            mesh.indices?.let {indices ->
                (0 until indices.size).forEach {
                    index(indices[it])
                }
            }
        }
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