package com.zakgof.korender.impl.model.terrain

import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.impl.engine.HeightFieldDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.MeshAttributes.B1
import com.zakgof.korender.impl.geometry.MeshAttributes.B2
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.Vec3Getter
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalTerrainMaterial
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlin.math.floor

internal class Clipmaps(val declaration: HeightFieldDeclaration) : AutoCloseable {

    private val center: InternalMeshDeclaration
    private val ring = mutableMapOf<Offset, InternalMeshDeclaration>()
    private val inner = 2 * declaration.hg + 3

    init {
        val outer = inner * 2
        center = declaration.nodeContext.customMesh(
            "${declaration.id}-center",
            (outer + 1) * (outer + 1),
            outer * outer * 6,
            B1, B2
        ) {
            buildBlock(declaration.hg, null)
        }
        for (ox in 0..1) {
            for (oz in 0..1) {
                ring[Offset(ox, oz)] = declaration.nodeContext.customMesh(
                    "${declaration.id}-ring-$ox-$oz",
                    (outer + 1) * (outer + 1),
                    (outer * outer - inner * inner) * 6,
                    B1, B2
                ) {
                    buildBlock(declaration.hg, Offset(ox, oz))
                }
            }
        }
    }

    private fun MeshInitializer.buildBlock(hg: Int, offset: Offset?) {
        // Grid
        for (x in 0..inner * 2) {
            for (z in 0..inner * 2) {
                p(x, z)
            }
        }
        // Bulk
        val bsize = inner * 2 + 1
        for (x in 0..<inner * 2) {
            for (z in 0..<inner * 2) {
                if (offset == null || (x < 1 + hg + offset.x || x > hg + inner + offset.x || z < 1 + hg + offset.z || z > hg + inner + offset.z)) {
                    val b = x * bsize + z
                    index(b, b + 1, b + bsize + 1, b, b + bsize + 1, b + bsize)
                }
            }
        }
    }

    private fun MeshInitializer.p(x: Int, z: Int) {
        attr(B1, x.toByte())
        attr(B2, z.toByte())
    }

    private fun meshes(camPos: Vec3): List<Me> {

        val posx = camPos.x / declaration.cellSize
        val posz = camPos.z / declaration.cellSize

        fun Float.snap(power: Int, offset: Int = 0) = floor(this - offset).toInt() and (Int.MAX_VALUE shl power)
        fun Float.fract(power: Int, offset: Int = 0) = (this - offset - snap(power, offset)) / (1 shl power)

        val list = mutableListOf<Me>()

        var xpos = posx.snap(1, inner)
        var zpos = posz.snap(1, inner)

        val px = posx.fract(1, inner)
        val pz = posz.fract(1, inner)

        list += Me(
            center, Vec3(xpos.toFloat(), zpos.toFloat(), 1f),
            Vec3(px, pz, inner * 2f)
        )

        for (r in 1..declaration.rings) {
            val step = 1 shl r

            val theOffset = (declaration.hg + 1) * (step - 1) * 2 + inner

            val newxpos = posx.snap(r + 1, theOffset)
            val newzpos = posz.snap(r + 1, theOffset)

            val prx = posx.fract(r + 1, theOffset)
            val prz = posz.fract(r + 1, theOffset)

            val offs = Offset((xpos - newxpos) / step - 1 - declaration.hg, (zpos - newzpos) / step - 1 - declaration.hg)
            val rrr = ring[offs]
            rrr?.let {
                list += Me(
                    it,
                    Vec3(newxpos.toFloat(), newzpos.toFloat(), step.toFloat()),
                    Vec3(prx, prz, inner * 2f)
                )
            }
            xpos = newxpos
            zpos = newzpos
        }
        return list
    }

    fun build(sceneDeclaration: SceneDeclaration) {
        val tiles = meshes(declaration.frameScope.camera.position)
        tiles.forEach { tile ->
            // TODO overhead!!!
            val modifier = TerrainMaterialModifier(tile, declaration.hg.toFloat() - 1f, declaration.cellSize)
            val material = InternalTerrainMaterial(modifier).apply(declaration.block)
            val rd = RenderableDeclaration(
                material,
                tile.mesh,
                Transform.IDENTITY,
                false,
                declaration.nodeContext
            )
            sceneDeclaration.append(rd)
        }
    }

    override fun close() {}

    private data class Offset(val x: Int, val z: Int)

    class Me(val mesh: InternalMeshDeclaration, val offsetAndScale: Vec3, val antipop: Vec3)
}

internal class TerrainMaterialModifier(
    val tile: Clipmaps.Me,
    val antipopSpan: Float,
    val cellSize: Float,
) : InternalMaterialModifier(
    "tileOffsetAndScale" to Vec3Getter<TerrainMaterialModifier> { it.tile.offsetAndScale },
    "antipop" to Vec3Getter<TerrainMaterialModifier> { it.tile.antipop },
    "antipopSpan" to FloatGetter<TerrainMaterialModifier> { it.antipopSpan },
    "cell" to FloatGetter<TerrainMaterialModifier> { it.cellSize }
)
