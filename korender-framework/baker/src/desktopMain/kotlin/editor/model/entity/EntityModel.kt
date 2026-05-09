package editor.model.entity

import com.zakgof.korender.math.Vec3
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class EntityModel(
    val name: String,
    val filename: String,
    val defaultScale: Float = 1f,
    val id: String = Uuid.generateV7().toHexDashString(),
) {
    val points: MutableList<Vec3> = mutableListOf()
}