package com.zakgof.korender.examples.qhull

import com.zakgof.korender.math.Vec3
import kotlin.math.abs

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

        val normal: Vec3 = ((b - a) % (c - a)).normalize()

        fun isAbove(point: Vec3) = (point - a) * normal > 0

        fun distance(point: Vec3) = abs((point - a) * normal)

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
            Face(ia, ib, ic),
            Face(ia, ib, id),
            Face(ia, ic, id),
            Face(ib, ic, id)
        )

        // Step 3: Collect points outside the hull for each face
        val outsidePoints = faces.associateWith { face ->
            points.indices.filter { face.isAbove(points[it]) }.toMutableList()
        }.toMutableMap()

        // Step 4: QuickHull iteration
        while (outsidePoints.isNotEmpty()) {
            val (face, exteriorPoints) = outsidePoints.filter { it.value.isNotEmpty() }.maxByOrNull { it.value.size }!!
            val farthestPointIndex = exteriorPoints.maxByOrNull { face.distance(points[it]) }!!
            val farthestPoint = points[farthestPointIndex]

            // Update hull by removing the face and forming new faces with the farthest point
            val visibleFaces = faces.filter { it.isAbove(farthestPoint) }
            val horizonEdges = findHorizonEdges(visibleFaces, faces)

            // Remove visible faces from the hull
            faces.removeAll(visibleFaces)

            // Add new faces to the hull
            for ((edgeStart, edgeEnd) in horizonEdges) {
                faces.add(Face(edgeStart, edgeEnd, farthestPointIndex))
            }

            // Reassign points outside the removed faces and new faces
            visibleFaces.forEach { outsidePoints.remove(it) }
            for (newFace in faces) {
                if (newFace !in outsidePoints) {
                    outsidePoints[newFace] = mutableListOf()
                }
                val exteriors = exteriorPoints.filter { face.isAbove(points[it]) }
                outsidePoints[newFace]!! += exteriors
            }
        }

        // Step 5: Generate final QHMesh
        val faceIndexes = mutableListOf<Int>()
        val facePoints = faces.flatMap { face ->
            listOf(face.a, face.b, face.c)
        }.distinct()

        val pointToIndex = facePoints.withIndex().associate { it.value to it.index }
        for (face in faces) {
            faceIndexes.add(pointToIndex[face.a]!!)
            faceIndexes.add(pointToIndex[face.b]!!)
            faceIndexes.add(pointToIndex[face.c]!!)
        }

        val qhPoints = facePoints.map { QHPoint(it, Vec3.ZERO) } // Normal can be computed later
        return QHMesh(qhPoints, faceIndexes)
    }


    private fun findFourthPoint(ia: Int, ib: Int, ic: Int): Int {
        val planeNormal = Face(ia, ib, ic).normal
        return points.indices.find { ((points[it] - points[ia]) * (planeNormal)) != 0f } ?: throw IllegalArgumentException("Points are coplanar")
    }

    private fun findHorizonEdges(visibleFaces: List<Face>, faces: List<Face>): List<Pair<Int, Int>> {
        val sharedEdges = mutableSetOf<Pair<Int, Int>>()
        for (face in visibleFaces) {
            val edges = listOf(
                face.ia to face.ib,
                face.ib to face.ic,
                face.ic to face.ia
            )
            for (edge in edges) {
                if (edge in sharedEdges) {
                    sharedEdges.remove(edge)
                } else {
                    sharedEdges.add(edge)
                }
            }
        }
        return sharedEdges.toList()
    }
}