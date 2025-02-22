package com.zakgof.korender.impl.geometry.terrain

import com.zakgof.korender.Attributes.B1
import com.zakgof.korender.Attributes.B2
import com.zakgof.korender.Attributes.PHI
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Vec3

class Clipmaps(korenderContext: KorenderContext, id: String, m: Int, rings: Int) {

    private val center: MeshDeclaration
    private val ring = mutableMapOf<Offset, MeshDeclaration>()

    init {
        val centerSide = 1 shl m
        val nextLevSize = centerSide shr 1
        val gridOdd = centerSide - 1
        val gridEven = gridOdd - 1
        val vertices = gridOdd * gridOdd + 4 * nextLevSize
        val indices = gridEven * gridEven * 6 + nextLevSize * 3 * 4 + (nextLevSize - 1) * 6 * 4

        center = korenderContext.customMesh(id, vertices, indices, B1, B2, PHI) {
            // Grid
            val gridHalf = (gridOdd - 1) shr 1
            for (x in -gridHalf..gridHalf) {
                for (z in -gridHalf..gridHalf) {
                    attr(B1, x.toByte())
                    attr(B2, z.toByte())
                    attr(PHI, 0f) // TODO
                }
            }
            for (x in 0 until gridEven) {
                for (z in 0 until gridEven) {
                    val b = x * gridOdd + z
                    index(b, b + 1, b + gridOdd + 1, b, b + gridOdd + 1, b + gridOdd)
                }
            }
            // Belt
            for (a in 0 until nextLevSize) {
                attr(B1, (-nextLevSize + 2 * a).toByte())
                attr(B2, (-nextLevSize).toByte())
                attr(PHI, 0f)
            }
            for (a in 0 until nextLevSize) {
                attr(B1, (nextLevSize).toByte())
                attr(B2, (-nextLevSize + 2 * a).toByte())
                attr(PHI, 0f)
            }
            for (a in 0 until nextLevSize) {
                attr(B1, (nextLevSize - 2 * a).toByte())
                attr(B2, (nextLevSize).toByte())
                attr(PHI, 0f)
            }
            for (a in 0 until nextLevSize) {
                attr(B1, (-nextLevSize).toByte())
                attr(B2, (nextLevSize - 2 * a).toByte())
                attr(PHI, 0f)
            }
            val vbb = gridOdd * gridOdd
            for (a in 0 until nextLevSize) {
                index(vbb + a, a * 2 * gridOdd, vbb + a + 1)
            }
            for (a in 0 until nextLevSize) {
                index(vbb + nextLevSize + a, a * 2 + gridOdd * gridEven, vbb + nextLevSize + a + 1)
            }
            for (a in 0 until nextLevSize) {
                index(vbb + nextLevSize * 2 + a, gridOdd * gridOdd - 1 - a * gridOdd * 2, vbb + nextLevSize * 2 + a + 1)
            }
            for (a in 0 until nextLevSize - 1) {
                index(vbb + nextLevSize * 3 + a, gridEven - a * 2, vbb + nextLevSize * 3 + a + 1)
            }
            index(vbb + nextLevSize * 4 - 1, gridEven - (nextLevSize - 1) * 2, vbb)
            //
            for (a in 1..<nextLevSize) {
                index(vbb + a, (a * 2 - 2) * gridOdd, (a * 2 - 1) * gridOdd)
                index(vbb + a, (a * 2 - 1) * gridOdd, (a * 2 - 0) * gridOdd)
            }
            for (a in 1..<nextLevSize) {
                index(vbb + nextLevSize + a, (a * 2 - 2) + gridOdd * gridEven, (a * 2 - 1) + gridOdd * gridEven)
                index(vbb + nextLevSize + a, (a * 2 - 1) + gridOdd * gridEven, (a * 2 - 0) + gridOdd * gridEven)
            }
            for (a in 1..<nextLevSize) {
                index(vbb + nextLevSize * 2 + a, gridOdd * gridOdd - 1 - (a * 2 - 2) * gridOdd, gridOdd * gridOdd - 1 - (a * 2 - 1) * gridOdd)
                index(vbb + nextLevSize * 2 + a, gridOdd * gridOdd - 1 - (a * 2 - 1) * gridOdd, gridOdd * gridOdd - 1 - (a * 2 - 0) * gridOdd)
            }
            for (a in 1..<nextLevSize) {
                index(vbb + nextLevSize * 3 + a, gridEven - (a * 2 - 2), gridEven - (a * 2 - 1))
                index(vbb + nextLevSize * 3 + a, gridEven - (a * 2 - 1), gridEven - (a * 2 - 0))
            }

        }
    }

    fun meshes(position: Vec3): List<Me> {
        return listOf(Me(center, Vec3(1f, 1f, 1f)))
    }

    data class Offset(val x: Int, val z: Int)

    class Me(val mesh: MeshDeclaration, val offsetAndScale: Vec3)
}