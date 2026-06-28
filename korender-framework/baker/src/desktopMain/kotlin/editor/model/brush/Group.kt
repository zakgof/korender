package editor.model.brush

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Group (
    val name: String,
    val brushIds: Set<String>,
    val id: String = Uuid.generateV7().toString()
)