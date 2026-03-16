package editor.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Material(
    val name: String,
    val colorTexture: TexId? = null,
    val baseColor: Color = White,
    val id: String = Uuid.generateV7().toHexDashString(),
    val fitToFace: Boolean = false
) {
    companion object {
        val generic: Material = Material("Generic", null, Blue, "generic")
    }
}

@Serializable
data class TexId(
    val path: String,
)