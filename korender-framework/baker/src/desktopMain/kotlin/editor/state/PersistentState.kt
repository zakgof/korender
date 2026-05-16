package editor.state

import kotlinx.serialization.Serializable

@Serializable
data class PersistentState(
    val lastDir: String?,
)