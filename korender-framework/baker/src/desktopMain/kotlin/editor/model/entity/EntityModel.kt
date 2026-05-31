package editor.model.entity

import com.zakgof.korender.math.Vec3
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class EntityModel(
    val name: String,
    val filename: String,
    val points: List<Vec3>,
    val defaultScale: Float = 1f,
    val keepProportions: Boolean = true,
    val id: String = Uuid.generateV7().toHexDashString(),
)