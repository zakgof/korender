package editor.model

import com.zakgof.korender.math.Vec3
import kotlin.math.max
import kotlin.math.min

class BoundingBox(val min: Vec3, val max: Vec3) {
    val center = (min + max) * 0.5f

    fun merge(that: BoundingBox) = BoundingBox(
        Vec3(min(this.min.x, that.min.x), min(this.min.y, that.min.y), min(this.min.z, that.min.z)),
        Vec3(max(this.max.x, that.max.x), max(this.max.y, that.max.y), max(this.max.z, that.max.z))
    )
}