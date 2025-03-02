package com.zakgof.korender.impl.geometry.terrain

import com.zakgof.korender.Attributes.B1
import com.zakgof.korender.Attributes.B2
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Vec3
import kotlin.math.floor

class Clipmaps(korenderContext: KorenderContext, id: String, private val hg: Int, private val rings: Int) {

    private val center: MeshDeclaration
    private val ring = mutableMapOf<Offset, MeshDeclaration>()
    private val inner = 2 * hg + 3

    init {
        val outer = inner * 2
        center = korenderContext.customMesh(
            "$id-center",
            (outer + 1) * (outer + 1),
            // outer * outer * 6 + inner * 3 * 4 + (inner - 1) * 6 * 4,
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

    fun meshes(position: Vec3): List<Me> {

        fun Float.snap(power: Int, offset: Int = 0) = floor(this - offset).toInt() and (Int.MAX_VALUE shl power)
        fun Float.fract(power: Int, offset: Int = 0) = (this - offset - snap(power, offset)) / (1 shl power)

        val list = mutableListOf<Me>()

        var xpos = position.x.snap(1, inner)
        var zpos = position.z.snap(1, inner)

        val px = position.x.fract(1, inner)
        val pz = position.z.fract(1, inner)

        list += Me(
            center, Vec3(xpos.toFloat(), zpos.toFloat(), 1f),
            px, pz
        )

        println("------------")
        for (r in 1..rings) {
            val step = 1 shl r

            val theOffset = (hg + 1) * (step - 1) * 2 + inner

            val newxpos = position.x.snap(r + 1, theOffset)
            val newzpos = position.z.snap(r + 1, theOffset)

            val prx = position.x.fract(r + 1, theOffset)
            val prz = position.z.fract(r + 1, theOffset)

            val offs = Offset((xpos - newxpos) / step - 1 - hg, (zpos - newzpos) / step - 1 - hg)
            list += Me(
                ring[offs]!!,
                Vec3(newxpos.toFloat(), newzpos.toFloat(), step.toFloat()),
                prx, prz
            )
            if (r == 3)
                println("ring $r: pz: $prz zpos: $newzpos")

            xpos = newxpos
            zpos = newzpos
        }
        return list
    }

    data class Offset(val x: Int, val z: Int)

    class Me(val mesh: MeshDeclaration, val offsetAndScale: Vec3, val px: Float, val pz: Float)
}