package editor.model.entity

import com.zakgof.korender.math.Vec3
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class EntityModel(
    val name: String,
    val ext: String,
    val bytes: ByteArray,
    val points: List<Vec3>,
    val defaultScale: Vec3 = Vec3(1f, 1f, 1f),
    val keepProportions: Boolean = true,
    val id: String = Uuid.generateV7().toHexDashString(),
)