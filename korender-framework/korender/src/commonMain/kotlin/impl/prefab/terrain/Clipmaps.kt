package com.zakgof.korender.impl.prefab.terrain

import com.zakgof.korender.Attributes.B1
import com.zakgof.korender.Attributes.B2
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.prefab.InternalPrefab
import com.zakgof.korender.math.Vec3
import kotlin.math.floor

internal class Clipmaps(korenderContext: KorenderContext, id: String, private val cellSize: Float, private val hg: Int, private val rings: Int)
    : InternalPrefab {

    private val center: MeshDeclaration
    private val ring = mutableMapOf<Offset, MeshDeclaration>()
    private val inner = 2 * hg + 3

    init {
        val outer = inner * 2
        center = korenderContext.customMesh(
            "$id-center",
            (outer + 1) * (outer + 1),
            outer * outer * 6,
            B1, B2
        ) {
            buildBlock(hg, null)
        }
        for (ox in 0..1) {
            for (oz in 0..1) {
                ring[Offset(ox, oz)] = korenderContext.customMesh(
                    "$id-ring-$ox-$oz",
                    (outer + 1) * (outer + 1),
                    (outer * outer - inner * inner) * 6,
                    B1, B2
                ) {
                    buildBlock(hg, Offset(ox, oz))
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

        val posx = camPos.x / cellSize
        val posz = camPos.z / cellSize

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

        for (r in 1..rings) {
            val step = 1 shl r

            val theOffset = (hg + 1) * (step - 1) * 2 + inner

            val newxpos = posx.snap(r + 1, theOffset)
            val newzpos = posz.snap(r + 1, theOffset)

            val prx = posx.fract(r + 1, theOffset)
            val prz = posz.fract(r + 1, theOffset)

            val offs = Offset((xpos - newxpos) / step - 1 - hg, (zpos - newzpos) / step - 1 - hg)
            list += Me(
                ring[offs]!!,
                Vec3(newxpos.toFloat(), newzpos.toFloat(), step.toFloat()),
                Vec3(prx, prz, inner * 2f)
            )
            xpos = newxpos
            zpos = newzpos
        }
        return list
    }

    override fun render(fc: FrameContext, vararg materialModifiers: MaterialModifier) = with(fc) {
        val tiles = meshes(fc.camera.position)
        tiles.forEach { tile ->
            Renderable(
                *materialModifiers,
                uniforms {
                    set("heightTexture", texture("terrain/base-terrain.jpg"))
                    set("tileOffsetAndScale", tile.offsetAndScale)
                    set("antipop", tile.antipop)
                    set("cell", cellSize)
                },
                fc.vertex("!shader/terrain.vert"),
                fc.defs("TERRAIN"),
                mesh = tile.mesh
            )
        }
    }

    private data class Offset(val x: Int, val z: Int)

    private class Me(val mesh: MeshDeclaration, val offsetAndScale: Vec3, val antipop: Vec3)
}