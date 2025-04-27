package com.zakgof.korender.examples.qhull

import com.zakgof.korender.math.Vec3

class QHMesh(val points: List<QHPoint>, val indexes: List<Int>)

class QHPoint(val pos: Vec3, val normal: Vec3)

class QuickHull(private val points: List<Vec3>) {

    inner class Face(val ia: Int, val ib: Int, val ic: Int) {

        val a: Vec3
            get() = points[ia]
        val b: Vec3
            get() = points[ib]
        val c: Vec3
            get() = points[ic]
        val vertexi: List<Int>
            get() = listOf(ia, ib, ic)

        val normal: Vec3 = ((b - a) % (c - a)).normalize()

        fun isAbove(point: Vec3) = distance(point) > 0

        fun distance(point: Vec3) = (point - a) * normal

        fun edges() = setOf(ia to ib, ib to ic, ic to ia)

    }

    fun run(): QHMesh {
        if (points.size < 4) throw IllegalArgumentException("At least 4 points are required for 3D convex hull")

        // Step 1: Find the extremes along x, y, and z axes
        val iminX = points.indices.minByOrNull { points[it].x }!!
        val imaxX = points.indices.maxByOrNull { points[it].x }!!
        val iminY = points.indices.minByOrNull { points[it].y }!!
        val imaxY = points.indices.maxByOrNull { points[it].y }!!

        // Step 2: Form initial tetrahedron
        val initialTetrahedron = listOf(iminX, imaxX, iminY, imaxY).distinct()
        if (initialTetrahedron.size < 4) throw IllegalArgumentException("Points are coplanar or degenerate")

        // Create a tetrahedron with four non-coplanar points
        val ia = initialTetrahedron[0]
        val ib = initialTetrahedron[1]
        val ic = initialTetrahedron[2]
        val id = findFourthPoint(ia, ib, ic)

        // Add initial hull faces
        val faces = mutableListOf(
            Face(ia, ic, ib),
            Face(ia, ib, id),
            Face(ib, ic, id),
            Face(ic, ia, id)
        )

        val centroid = (points[ia] + points[ib] + points[ic] + points[id]) * 0.25f
        faces.forEach {
            val d = it.distance(centroid)
            println("Distance to centroid $d")
        }

        do {
            val (face, pt) = faces.flatMap { f -> points.indices.map { p -> (f to p) } }
                .maxByOrNull { it.first.distance(points[it.second]) }!!
            val maxDist = face.distance(points[pt])
            println("Max distance: $maxDist")
            if (maxDist < 1e-4f)
                break
            appendPoint(faces, pt)

        } while (true)

        val originalIndexesFromHull = faces
            .flatMap { it.vertexi }
            .distinct()
            .sorted()

        val originalIndexToIndex = originalIndexesFromHull
            .withIndex()
            .associate { it.value to it.index }

        val normalAccumulators = Array(originalIndexesFromHull.size) { Vec3.ZERO }
        faces.forEach {
            val n = it.normal
            normalAccumulators[originalIndexToIndex[it.ia]!!] += n
            normalAccumulators[originalIndexToIndex[it.ib]!!] += n
            normalAccumulators[originalIndexToIndex[it.ic]!!] += n
        }

        val div = 1f / originalIndexesFromHull.size
        val cntrd = originalIndexesFromHull.indices.map {points[originalIndexesFromHull[it]]}
            .fold(Vec3.ZERO) { acc, it -> (acc + it) * div}

        val vertices = originalIndexesFromHull.indices.map {
            QHPoint(points[originalIndexesFromHull[it]] - cntrd, normalAccumulators[it].normalize())
        }
        val indexes = faces.flatMap { it.vertexi }
            .map { originalIndexToIndex[it]!! }
        return QHMesh(vertices, indexes)
    }

    private fun appendPoint(faces: MutableList<Face>, index: Int) {
        val pt = points[index]
        val visibleFaces = faces
            .filter { it.ia != index && it.ib != index && it.ic != index }
            .filter { it.isAbove(pt) }

        val horizonEdges = mutableSetOf<Pair<Int, Int>>()
        visibleFaces.flatMap { it.edges() }
            .forEach {
                if (!horizonEdges.remove(it.second to it.first)) {
                    horizonEdges.add(it)
                }
            }
        faces -= visibleFaces.toSet()
        faces += horizonEdges.map { Face(it.first, it.second, index) }.toSet()
    }

    private fun findFourthPoint(ia: Int, ib: Int, ic: Int): Int {
        val face = Face(ia, ib, ic)
        return points.indices.find {
            face.distance(points[it]) > 0
        } ?: throw IllegalArgumentException("Points are poor")
    }

}