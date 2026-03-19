package editor.model

import com.zakgof.korender.math.Vec3
import kotlin.math.max
import kotlin.math.min

class BoundingBox(val min: Vec3, val max: Vec3) {

    val center = (min + max) * 0.5f

    val size = (max - min)

    infix fun merge(that: BoundingBox) = BoundingBox(
        Vec3(min(this.min.x, that.min.x), min(this.min.y, that.min.y), min(this.min.z, that.min.z)),
        Vec3(max(this.max.x, that.max.x), max(this.max.y, that.max.y), max(this.max.z, that.max.z))
    )

    fun longestAxis() =
        when {
            (size.x > size.y && size.x > size.z) -> Vec3.X
            (size.z > size.y && size.z > size.x) -> Vec3.Y
            else -> Vec3.Z
        }

    companion object {
        fun from(c1: Vec3, c2: Vec3): BoundingBox =
            BoundingBox(
                Vec3(min(c1.x, c2.x), min(c1.y, c2.y), min(c1.z, c2.z)),
                Vec3(max(c1.x, c2.x), max(c1.y, c2.y), max(c1.z, c2.z))
            )
    }
}