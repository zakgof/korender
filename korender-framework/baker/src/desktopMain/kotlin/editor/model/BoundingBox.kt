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

    fun intersects(other: BoundingBox): Boolean {
        return max.x >= other.min.x &&
                min.x <= other.max.x &&
                max.y >= other.min.y &&
                min.y <= other.max.y &&
                max.z >= other.min.z &&
                min.z <= other.max.z
    }

    fun scale(olbBB: BoundingBox, newBB: BoundingBox): BoundingBox {
        val scale = Vec3(
            newBB.size.x / olbBB.size.x,
            newBB.size.y / olbBB.size.y,
            newBB.size.z / olbBB.size.z
        )
        val shift = center multpercomp (Vec3(1f, 1f, 1f) - scale)
        return BoundingBox(
            (min multpercomp scale) + shift,
            (max multpercomp scale) + shift
        )
    }

    fun resize(newSize: Vec3) =
        BoundingBox(
            center - (newSize * 0.5f),
            center + (newSize * 0.5f)
        )

    fun move(newCenter: Vec3) =
        BoundingBox(
            min + newCenter - center,
            max + newCenter - center
        )

    fun corners(): List<Vec3> {
        return listOf(
            Vec3(min.x, min.y, min.z),
            Vec3(max.x, min.y, min.z),
            Vec3(min.x, max.y, min.z),
            Vec3(max.x, max.y, min.z),
            Vec3(min.x, min.y, max.z),
            Vec3(max.x, min.y, max.z),
            Vec3(min.x, max.y, max.z),
            Vec3(max.x, max.y, max.z)
        )
    }

    companion object {
        fun from(c1: Vec3, c2: Vec3) =
            BoundingBox(
                Vec3(min(c1.x, c2.x), min(c1.y, c2.y), min(c1.z, c2.z)),
                Vec3(max(c1.x, c2.x), max(c1.y, c2.y), max(c1.z, c2.z))
            )

        fun from(points: Collection<Vec3>) =
            require(points.isNotEmpty()) { "Cannot build BoundingBox from an empty collection" }.let {
                BoundingBox(
                    Vec3(
                        points.minOf { p -> p.x },
                        points.minOf { p -> p.y },
                        points.minOf { p -> p.z }
                    ),
                    Vec3(
                        points.maxOf { p -> p.x },
                        points.maxOf { p -> p.y },
                        points.maxOf { p -> p.z }
                    )
                )
            }
    }
}
