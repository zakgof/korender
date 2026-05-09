package editor.model.entity

import com.zakgof.korender.baker.editor.model.Boundable
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class EntityInstance(
    val name: String,
    val modelId: String,
    val points: List<Vec3>,
    val transform: Transform,
    val id: String = Uuid.generateV7().toHexDashString(),
) : Boundable {
    constructor(name: String, model: EntityModel, transform: Transform) :
            this(name, model.id, model.points, transform)

    override val bb by lazy { BoundingBox.from(points.map { transform * it }) }
}

