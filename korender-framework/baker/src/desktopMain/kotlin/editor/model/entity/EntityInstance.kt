package editor.model.entity

import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class EntityInstance(
    val name: String,
    val modelId: String,
    val transform: Transform,
    val bb: BoundingBox,
    val id: String = Uuid.generateV7().toHexDashString(),
) {
    constructor(name: String, model: EntityModel, transform: Transform) :
            this(name, model.id, transform, calculateBB(model.points, transform))
}

private fun calculateBB(points: List<Vec3>, transform: Transform) =
    BoundingBox.from(points.map { transform * it })


