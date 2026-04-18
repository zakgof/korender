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

    data class Cylinder(val sides: Int = 16) : CreatorShape {
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
            for (i in 0 until sides) {
                val a = i * step
                val b = (i + 1) * step
                val bottomA = ringPoint(minY, a)
                val topA = ringPoint(maxY, a)
                val bottomB = ringPoint(minY, b)

                var plane = Plane(bottomA, topA, bottomB)
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

    data class Cone(val sides: Int = 8) : CreatorShape {
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

            for (i in 0 until sides) {
                val a = i * step
                val b = (i + 1) * step
                val baseA = ringPoint(a)
                val baseB = ringPoint(b)

                var plane = Plane(apex, baseA, baseB)
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

    data class Sphere(val slices: Int = 8, val sectors: Int = 8) : CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            val center = bb.center
            val size = bb.size
            val rx = size.x * 0.5f
            val ry = size.y * 0.5f
            val rz = size.z * 0.5f

            val faces = mutableListOf<Face>()

            val latStep = PI.toFloat() / slices
            val lonStep = (2f * PI.toFloat()) / sectors

            fun point(lat: Float, lon: Float) = Vec3(
                center.x + rx * cos(lat) * cos(lon),
                center.y + ry * sin(lat),
                center.z + rz * cos(lat) * sin(lon)
            )

            fun addFace(p0: Vec3, p1: Vec3, p2: Vec3) {
                var plane = Plane(p0, p1, p2)
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
