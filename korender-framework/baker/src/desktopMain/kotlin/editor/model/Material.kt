package editor.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Material(
    val name: String,
    val colorTexture: String? = null,
    val baseColor: Color = White,
    val id: String = Uuid.generateV7().toHexDashString(),
    val fitToFace: Boolean = false,
    val stochastic: Boolean = false,
    val triplanar: Boolean = false,
    val scale: Float = 1f
) {
    companion object {
        val generic: Material = Material("Generic", null, Blue, "generic")
    }
}