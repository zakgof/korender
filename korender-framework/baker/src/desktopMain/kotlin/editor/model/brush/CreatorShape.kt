package editor.model.brush

import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

sealed interface CreatorShape {

    fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face>

    object Box : CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean) =
            Plane.cube(bb).map { Face(it, materialId, fitToFace) }
    }

    data class Cylinder(val sides: Int = 8) : CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val cx = (bb.min.x + bb.max.x) / 2f
            val cz = (bb.min.z + bb.max.z) / 2f
            val rx = (bb.max.x - bb.min.x) / 2f
            val rz = (bb.max.z - bb.min.z) / 2f
            val minY = bb.min.y
            val maxY = bb.max.y

            val faces = mutableListOf<Face>()

            val step = (2.0 * PI) / sides

            // side faces
            val centerY = (minY + maxY) / 2f
            val center = Vec3(cx, centerY, cz)
            for (i in 0 until sides) {
                val a = i * step
                val b = (i + 1) * step
                val bottomA = Vec3((cx + (rx * cos(a)).toFloat()), minY, (cz + (rz * sin(a)).toFloat()))
                val topA = Vec3((cx + (rx * cos(a)).toFloat()), maxY, (cz + (rz * sin(a)).toFloat()))
                val bottomB = Vec3((cx + (rx * cos(b)).toFloat()), minY, (cz + (rz * sin(b)).toFloat()))

                var plane = Plane(bottomA, topA, bottomB)
                // ensure plane normal points outward from cylinder center
                val midAngle = (a + b) / 2.0
                val midPoint = Vec3((cx + (rx * cos(midAngle)).toFloat()), centerY, (cz + (rz * sin(midAngle)).toFloat()))
                if (plane.normal dot (midPoint - center) < 0f) plane = plane.invert()
                faces += Face(plane, materialId, fitToFace)
            }

            // top cap (normal pointing up)
            val topCenter = Vec3(cx, maxY, cz)
            val topA = 0.0
            val topB = step
            val topPu = Vec3((cx + (rx * cos(topA)).toFloat()), maxY, (cz + (rz * sin(topA)).toFloat()))
            val topPv = Vec3((cx + (rx * cos(topB)).toFloat()), maxY, (cz + (rz * sin(topB)).toFloat()))
            var topPlane = Plane(topCenter, topPu, topPv)
            if (topPlane.normal.y < 0f) topPlane = topPlane.invert()
            faces += Face(topPlane, materialId, fitToFace)

            // bottom cap (normal pointing down)
            val bottomCenter = Vec3(cx, minY, cz)
            val bottomPu = Vec3((cx + (rx * cos(topB)).toFloat()), minY, (cz + (rz * sin(topB)).toFloat()))
            val bottomPv = Vec3((cx + (rx * cos(topA)).toFloat()), minY, (cz + (rz * sin(topA)).toFloat()))
            var bottomPlane = Plane(bottomCenter, bottomPu, bottomPv)
            if (bottomPlane.normal.y > 0f) bottomPlane = bottomPlane.invert()
            faces += Face(bottomPlane, materialId, fitToFace)

            return faces
        }
    }

    data class Cone(val sides: Int = 8) : CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            throw NotImplementedError()
        }
    }

    data class Sphere(val slices: Int = 8, val sectors: Int = 8) : CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            throw NotImplementedError()
        }
    }
}