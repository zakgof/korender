@file:OptIn(ExperimentalUuidApi::class)

package editor.model.brush

import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface CreatorShape {

    val name: String

    fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face>

    object Box : CreatorShape {

        override val name = "Box"

        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean) =
            Plane.cube(bb).map { Face(it, materialId, fitToFace) }
    }

    object RightWedge : CreatorShape {

        override val name = "Wedge"

        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val center = bb.center
            val min = bb.min
            val max = bb.max

            val tr0 = Vec3(max.x, max.y, min.z)
            val br0 = Vec3(max.x, min.y, min.z)
            val bl0 = Vec3(min.x, min.y, min.z)

            val tr1 = Vec3(max.x, max.y, max.z)
            val br1 = Vec3(max.x, min.y, max.z)
            val bl1 = Vec3(min.x, min.y, max.z)

            val faces = mutableListOf<Face>()
            fun outward(plane: Plane, sample: Vec3): Plane =
                if (plane.normal dot (sample - center) < 0f) plane.invert() else plane

            var plane = Plane(tr0, br0, bl0)
            plane = outward(plane, (tr0 + br0 + bl0) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(tr1, bl1, br1)
            plane = outward(plane, (tr1 + bl1 + br1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(tr0, tr1, br1)
            plane = outward(plane, (tr0 + tr1 + br1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(tr0, bl0, bl1)
            plane = outward(plane, (tr0 + bl0 + bl1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(bl0, br0, br1)
            plane = outward(plane, (bl0 + br0 + br1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            return faces
        }
    }

    object SymmetricWedge : CreatorShape {

        override val name = "SymWedge"

        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val center = bb.center
            val min = bb.min
            val max = bb.max

            val apex0 = Vec3(center.x, max.y, min.z)
            val apex1 = Vec3(center.x, max.y, max.z)
            val left0 = Vec3(min.x, min.y, min.z)
            val right0 = Vec3(max.x, min.y, min.z)
            val left1 = Vec3(min.x, min.y, max.z)
            val right1 = Vec3(max.x, min.y, max.z)

            val faces = mutableListOf<Face>()
            fun outward(plane: Plane, sample: Vec3): Plane =
                if (plane.normal dot (sample - center) < 0f) plane.invert() else plane

            var plane = Plane(apex0, right0, left0)
            plane = outward(plane, (apex0 + right0 + left0) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(apex1, left1, right1)
            plane = outward(plane, (apex1 + left1 + right1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(apex0, apex1, left1)
            plane = outward(plane, (apex0 + apex1 + left1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(apex1, apex0, right0)
            plane = outward(plane, (apex1 + apex0 + right0) / 3f)
            faces += Face(plane, materialId, fitToFace)

            plane = Plane(left0, right0, right1)
            plane = outward(plane, (left0 + right0 + right1) / 3f)
            faces += Face(plane, materialId, fitToFace)

            return faces
        }
    }

    data class Cylinder(val sides: Int = 16) : CreatorShape {

        override val name = "Cylinder"

        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val center = bb.center
            val size = bb.size
            val rx = size.x * 0.5f
            val rz = size.z * 0.5f
            val minY = bb.min.y
            val maxY = bb.max.y

            val faces = mutableListOf<Face>()
            val step = (2f * PI.toFloat()) / sides
            fun ringPoint(y: Float, a: Float) =
                Vec3(center.x + rx * cos(a), y, center.z + rz * sin(a))
            fun outward(plane: Plane, sample: Vec3): Plane =
                if (plane.normal dot (sample - center) < 0f) plane.invert() else plane

            // side faces
            val smoothId = Uuid.generateV7()
            for (i in 0 until sides) {
                val a = i * step
                val b = (i + 1) * step
                val bottomA = ringPoint(minY, a)
                val topA = ringPoint(maxY, a)
                val bottomB = ringPoint(minY, b)

                var plane = Plane(bottomA, topA, bottomB, smoothId)
                val midAngle = (a + b) * 0.5f
                val midPoint = ringPoint((minY + maxY) * 0.5f, midAngle)
                plane = outward(plane, midPoint)
                faces += Face(plane, materialId, fitToFace)
            }

            // top cap (normal pointing up)
            val topCenter = Vec3(center.x, maxY, center.z)
            val topA = 0f
            val topB = step
            val topPu = ringPoint(maxY, topA)
            val topPv = ringPoint(maxY, topB)
            var topPlane = Plane(topCenter, topPu, topPv)
            topPlane = outward(topPlane, topCenter)
            faces += Face(topPlane, materialId, fitToFace)

            // bottom cap (normal pointing down)
            val bottomCenter = Vec3(center.x, minY, center.z)
            val bottomPu = ringPoint(minY, topB)
            val bottomPv = ringPoint(minY, topA)
            var bottomPlane = Plane(bottomCenter, bottomPu, bottomPv)
            bottomPlane = outward(bottomPlane, bottomCenter)
            faces += Face(bottomPlane, materialId, fitToFace)

            return faces
        }
    }

    data class Cone(val sides: Int = 16) : CreatorShape {

        override val name = "Cone"

        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val center = bb.center
            val size = bb.size
            val rx = size.x * 0.5f
            val rz = size.z * 0.5f
            val minY = bb.min.y
            val maxY = bb.max.y
            val apex = Vec3(center.x, maxY, center.z)

            val faces = mutableListOf<Face>()
            val step = (2f * PI.toFloat()) / sides
            fun ringPoint(a: Float) =
                Vec3(center.x + rx * cos(a), minY, center.z + rz * sin(a))
            fun outward(plane: Plane, sample: Vec3): Plane =
                if (plane.normal dot (sample - center) < 0f) plane.invert() else plane

            val smoothId = Uuid.generateV7()
            for (i in 0 until sides) {
                val a = i * step
                val b = (i + 1) * step
                val baseA = ringPoint(a)
                val baseB = ringPoint(b)

                var plane = Plane(apex, baseA, baseB, smoothId)
                val midPoint = (apex + baseA + baseB) / 3f
                plane = outward(plane, midPoint)
                faces += Face(plane, materialId, fitToFace)
            }

            // bottom cap (normal pointing down)
            val bottomCenter = Vec3(center.x, minY, center.z)
            val bottomPu = ringPoint(step)
            val bottomPv = ringPoint(0f)
            var bottomPlane = Plane(bottomCenter, bottomPu, bottomPv)
            bottomPlane = outward(bottomPlane, bottomCenter)
            faces += Face(bottomPlane, materialId, fitToFace)

            return faces
        }
    }

    data class Sphere(val slices: Int = 12, val sectors: Int = 12) : CreatorShape {

        override val name = "Sphere"

        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val center = bb.center
            val size = bb.size
            val rx = size.x * 0.5f
            val ry = size.y * 0.5f
            val rz = size.z * 0.5f

            val faces = mutableListOf<Face>()

            val latStep = PI.toFloat() / slices
            val lonStep = (2f * PI.toFloat()) / sectors

            val smoothId = Uuid.generateV7()

            fun point(lat: Float, lon: Float) = Vec3(
                center.x + rx * cos(lat) * cos(lon),
                center.y + ry * sin(lat),
                center.z + rz * cos(lat) * sin(lon)
            )

            fun addFace(p0: Vec3, p1: Vec3, p2: Vec3) {
                var plane = Plane(p0, p1, p2, smoothId)
                val midPoint = (p0 + p1 + p2) / 3f
                if (plane.normal dot (midPoint - center) < 0f) plane = plane.invert()
                faces += Face(plane, materialId, fitToFace)
            }

            for (i in 0 until slices) {
                val lat0 = -PI.toFloat() * 0.5f + i * latStep
                val lat1 = lat0 + latStep

                for (j in 0 until sectors) {
                    val lon0 = j * lonStep
                    val lon1 = lon0 + lonStep

                    if (i == 0) {
                        val pole = point(lat0, 0f)
                        val p1 = point(lat1, lon0)
                        val p2 = point(lat1, lon1)
                        addFace(pole, p1, p2)
                    } else if (i == slices - 1) {
                        val p1 = point(lat0, lon0)
                        val p2 = point(lat0, lon1)
                        val pole = point(lat1, 0f)
                        addFace(p1, pole, p2)
                    } else {
                        val p00 = point(lat0, lon0)
                        val p01 = point(lat0, lon1)
                        val p10 = point(lat1, lon0)
                        val p11 = point(lat1, lon1)

                        addFace(p00, p10, p11)
                        addFace(p00, p11, p01)
                    }
                }
            }

            return faces
        }
    }
}
