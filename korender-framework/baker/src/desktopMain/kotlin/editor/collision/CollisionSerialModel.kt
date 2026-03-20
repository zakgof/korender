package com.zakgof.korender.baker.editor.collision

import kotlinx.serialization.Serializable

class CollisionSerialModel {

    @Serializable
    data class BvhNode (
        val bb: BoundingBox,
        val left: BvhNode?,
        val right: BvhNode?,
        val brushes: Collection<Brush>?
    ) {
        constructor(bvhNode: com.zakgof.korender.baker.editor.collision.BvhNode) : this(
            bb = BoundingBox(
                bvhNode.bb.min.x,
                bvhNode.bb.min.y,
                bvhNode.bb.min.z,
                bvhNode.bb.max.x,
                bvhNode.bb.max.y,
                bvhNode.bb.max.z
            ),
            left = bvhNode.left?.let { BvhNode(it) },
            right = bvhNode.right?.let { BvhNode(it) },
            brushes = bvhNode.brushes?.map { brush ->
                Brush(
                    planes = brush.faces.map { face ->
                        val n = face.plane.normal
                        Plane(n.x, n.y, n.z, face.plane.d)
                    }
                )
            }
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
    )
    @Serializable
    data class Brush (
        val planes: List<Plane>,
    )
    @Serializable
    data class Plane (
        val normalX: Float,
        val normalY: Float,
        val normalZ: Float,
        val d: Float
    )
}
