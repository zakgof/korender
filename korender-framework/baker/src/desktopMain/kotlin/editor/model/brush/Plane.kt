package editor.model.brush

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import editor.model.rotateVec
import kotlin.math.sqrt

data class Plane(
    val p0: Vec3,
    val pu: Vec3,
    val pv: Vec3,
) {

    companion object {
        fun cube(bb: BoundingBox) =
            listOf(
                Plane(Vec3(bb.min.x, bb.min.y, bb.min.z), Vec3(bb.min.x, bb.min.y, bb.max.z), Vec3(bb.min.x, bb.max.y, bb.min.z)),
                Plane(Vec3(bb.max.x, bb.min.y, bb.max.z), Vec3(bb.max.x, bb.min.y, bb.min.z), Vec3(bb.max.x, bb.max.y, bb.max.z)),
                Plane(Vec3(bb.min.x, bb.min.y, bb.max.z), Vec3(bb.max.x, bb.min.y, bb.max.z), Vec3(bb.min.x, bb.max.y, bb.max.z)),
                Plane(Vec3(bb.max.x, bb.min.y, bb.min.z), Vec3(bb.min.x, bb.min.y, bb.min.z), Vec3(bb.max.x, bb.max.y, bb.min.z)),

                Plane(Vec3(bb.min.x, bb.min.y, bb.min.z), Vec3(bb.max.x, bb.min.y, bb.min.z), Vec3(bb.min.x, bb.min.y, bb.max.z)),
                Plane(Vec3(bb.min.x, bb.max.y, bb.max.z), Vec3(bb.max.x, bb.max.y, bb.max.z), Vec3(bb.min.x, bb.max.y, bb.min.z))
            )
    }

    val normal by lazy {
        ((pu - p0) cross (pv - p0)).normalize()
    }

    val d by lazy {
        -(normal dot p0)
    }

    override fun toString() = "n=$normal d=$d"

    fun distanceTo(p: Vec3) = normal * p + d

    fun translate(offset: Vec3): Plane =
        copy(p0 = p0 + offset, pu = pu + offset, pv = pv + offset)

    fun scale(oldBB: BoundingBox, newBB: BoundingBox): Plane {
        val scale = (newBB.max - newBB.min) divpercomp (oldBB.max - oldBB.min)
        fun xform(p: Vec3): Vec3 = newBB.min + ((p - oldBB.min) multpercomp scale)
        return copy(
            p0 = xform(p0),
            pu = xform(pu),
            pv = xform(pv)
        )
    }

    fun rotate(center: Vec3, axis: Vec3, angle: Float): Plane {
        fun xform(p: Vec3): Vec3 = center + rotateVec(p - center, axis, angle)
        return copy(
            p0 = xform(p0),
            pu = xform(pu),
            pv = xform(pv)
        )
    }

    fun tex(pos: Vec3, fitToFace: Boolean): Vec2 {
        val u = pu - p0
        val v = pv - p0
        val d = pos - p0

        val uu = u.dot(u)
        val uv = u.dot(v)
        val vv = v.dot(v)
        val du = d.dot(u)
        val dv = d.dot(v)

        val invDet = 1f / (uu * vv - uv * uv)

        val tu = (du * vv - dv * uv) * invDet
        val tv = (du * uv - dv * uu) * invDet + 1f

        return if (!fitToFace) {
            Vec2(
                tu * sqrt(uu),
                tv * sqrt(vv)
            )
        } else {
            Vec2(tu, tv)
        }
    }

    fun invert() = Plane(p0, pv, pu)
}
