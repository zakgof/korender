package com.zakgof.korender.impl.geometry.terrain

import com.zakgof.korender.Attributes.B1
import com.zakgof.korender.Attributes.B2
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Vec3
import kotlin.math.min
import kotlin.math.roundToInt

class Clipmaps(korenderContext: KorenderContext, id: String, private val hg: Int, private val rings: Int) {

    private val center: MeshDeclaration
    private val ring = mutableMapOf<Offset, MeshDeclaration>()
    private val inner = 2 * hg + 3

    init {
        val outer = inner * 2
        center = korenderContext.customMesh(
            "$id-center",
            (outer + 1) * (outer + 1),
            outer * outer * 6 + inner * 3 * 4 + (inner - 1) * 6 * 4,
            B1, B2, PHI
        ) {
            buildBlock(hg, null)
        }
        val ringVertices = (outer + 1) * (outer + 1)
        val ringIndices = (outer * outer - inner * inner) * 6 + inner * 3 * 4 + (inner - 1) * 6 * 4
        for (ox in 0..1) {
            for (oz in 0..1) {
                ring[Offset(ox, oz)] = korenderContext.customMesh(
                    "$id-ring-$ox-$oz",
                    ringVertices,
                    ringIndices,
                    B1, B2, PHI
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
        for (x in 1..<inner * 2 - 1) {
            for (z in 1..<inner * 2 - 1) {
                if (offset == null || (x < 1 + hg + offset.x || x > hg + inner + offset.x || z < 1 + hg + offset.z || z > hg + inner + offset.z)) {
                    val b = x * bsize + z
                    index(b, b + 1, b + bsize + 1, b, b + bsize + 1, b + bsize)
                }
            }
        }
        // Belt
        for (x in 0..<inner) {
            val b = x * 2 * bsize
            index(b + 0, b + 1 + bsize, b + 2 * bsize)
        }
        for (x in 0..<inner - 1) {
            val b = x * 2 * bsize
            index(b + 1 + bsize, b + 1 + bsize * 2, b + 2 * bsize, b + 2 * bsize, b + 1 + bsize * 2, b + 1 + bsize * 3)
        }

        for (z in 0..<inner) {
            val b = z * 2
            index(b + 0, b + 2, b + 1 + bsize)
        }
        for (z in 0..<inner - 1) {
            val b = z * 2
            index(b + 1 + bsize, b + 2, b + 2 + bsize, b + 2 + bsize, b + 2, b + bsize + 3)
        }

        for (x in 0..<inner) {
            val b = x * 2 * bsize + bsize - 1
            index(b + 0, b + 2 * bsize, b + bsize - 1)
        }
        for (x in 0..<inner - 1) {
            val b = x * 2 * bsize + 2 * bsize - 2
            index(b, b + 1 + bsize, b + 1 * bsize, b + 1 * bsize, b + 1 + bsize, b + 2 * bsize)
        }

        for (z in 0..<inner) {
            val b = bsize * (bsize - 1) + z * 2
            index(b + 0, b + 1 - bsize, b + 2)
        }
        for (z in 0..<inner - 1) {
            val b = bsize * (bsize - 2) + z * 2 + 1
            index(b, b + 1, b + bsize + 1, b + bsize + 1, b + 1, b + 2)
        }
    }

    private fun MeshInitializer.p(x: Int, z: Int) {
        attr(B1, x.toByte())
        attr(B2, z.toByte())

        val alpha = 1.3f / (hg + 1.0f)

        val m1 = (x * alpha).coerceIn(0f, 1f)
        val m2 = (((inner * 2f) - x) * alpha).coerceIn(0f, 1f)
        val n1 = (z * alpha).coerceIn(0f, 1f)
        val n2 = (((inner * 2f) - z) * alpha).coerceIn(0f, 1f)
        val m = min(m1, m2)
        val n = min(n1, n2)

        attr(PHI, min(m, n))

        println("x: $x  z: $z  phi: ${min(m, n)}")
    }

    fun meshes(position: Vec3): List<Me> {

        fun Float.snap(power: Int, offset: Int = 0) = (this.roundToInt() - offset) and (Int.MAX_VALUE shl power)

        val list = mutableListOf<Me>()

        var xpos = position.x.snap(1, inner)
        var zpos = position.z.snap(1, inner)

        list += Me(center, Vec3(xpos.toFloat(), zpos.toFloat(), 1f))

        for (r in 1..rings) {
            val step = 1 shl r
            val newxpos = xpos.toFloat().snap(r + 1, step * (hg + 1))
            val newzpos = zpos.toFloat().snap(r + 1, step * (hg + 1))

            val offs = Offset((xpos - newxpos) / step - 1 - hg, (zpos - newzpos) / step - 1 - hg)
            list += Me(
                ring[offs]!!,
                Vec3(newxpos.toFloat(), newzpos.toFloat(), step.toFloat())
            )

            xpos = newxpos
            zpos = newzpos
        }
        return list
    }

    data class Offset(val x: Int, val z: Int)

    class Me(val mesh: MeshDeclaration, val offsetAndScale: Vec3)
}