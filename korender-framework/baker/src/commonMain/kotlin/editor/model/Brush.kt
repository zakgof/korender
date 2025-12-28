package editor.model

import androidx.compose.ui.graphics.Color
import com.zakgof.korender.math.Vec3

data class Brush(
    val min: Vec3,
    val max: Vec3,
    val projectionColor: Color,
    val name: String,
    val id: String
) {
    val center: Vec3
        get() = (min + max) * 0.5f
}