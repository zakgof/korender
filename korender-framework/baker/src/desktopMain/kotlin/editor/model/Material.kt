package editor.model

import com.zakgof.korender.math.ColorRGBA.Companion.Blue
import com.zakgof.korender.math.ColorRGBA.Companion.White
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Material(
    val name: String,
    val colorTexture: TexId? = null,
    val baseColor: Long = White.toLong(),
    val id: String = Uuid.generateV7().toHexDashString(),
) {


    companion object {
        val generic: Material = Material("Generic", null, Blue.toLong(), "generic")
    }
}

@Serializable
data class TexId(
    val path: String
)
