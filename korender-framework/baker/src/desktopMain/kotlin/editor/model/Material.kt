package editor.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Material(
    val name: String,
    val colorTexture: Tex? = null,
    val baseColor: Color = White,
    val id: String = Uuid.generateV7().toHexDashString(),
    val fitToFace: Boolean = false,
    val stochastic: Boolean = false,
    val triplanar: Boolean = false,
    val scale: Float = 1f,
    val metallic: Float = 0f,
    val roughness: Float = 0.8f
) {
    companion object {
        val generic: Material = Material("Generic", null, Blue, "generic")
    }
}

class Tex(
    val name: String,
    val ext: String,
    val bytes: ByteArray
) {
    constructor(file: File) : this(file.name, file.extension.lowercase(), file.readBytes())
}