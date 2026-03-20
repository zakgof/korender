package com.zakgof.korender.baker.editor.walk

import kotlinx.serialization.Serializable
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox as ModelBoundingBox

class WalkSerialModel {

    @Serializable
    data class BvhNode (
        val bb: BoundingBox,
        val left: BvhNode?,
        val right: BvhNode?,
        val brushes: Collection<Brush>?
    ) {

        fun inflate(): WalkBvhNode =
            WalkBvhNode(
                bb = bb.inflate(),
                left = left?.inflate(),
                right = right?.inflate(),
                brushes = brushes?.map { it.inflate() }
            )

    }

    @Serializable
    data class BoundingBox (
        val minX: Float,
        val minY: Float,
        val minZ: Float,
        val maxX: Float,
        val maxY: Float,
        val maxZ: Float,
    ) {
        fun inflate(): editor.model.BoundingBox =
            ModelBoundingBox(
                Vec3(minX, minY, minZ),
                Vec3(maxX, maxY, maxZ)
            )
    }
    @Serializable
    data class Brush (
        val planes: List<Plane>,
    ) {
        fun inflate(): WalkBrush =
            WalkBrush(
                planes = planes.map { it.inflate() }
            )
    }
    @Serializable
    data class Plane (
        val normalX: Float,
        val normalY: Float,
        val normalZ: Float,
        val d: Float
    ) {
        fun inflate(): WalkPlane =
            WalkPlane(
                normal = Vec3(normalX, normalY, normalZ),
                d = d
            )
    }
}
