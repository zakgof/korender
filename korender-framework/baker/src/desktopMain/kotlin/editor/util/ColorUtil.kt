package editor.util

import androidx.compose.ui.graphics.Color
import editor.model.brush.Brush
import editor.model.entity.EntityInstance

fun Brush.color() = id.hashCode().color()

fun EntityInstance.color() = id.hashCode().color()

fun Int.color(): Color {
    val c1 = this and 0xFF
    val c2 = (this shr 8) and 0xFF
    val c3 = (this shr 16) and 0xFF

    return Color(80 + c1 * (255 - 80) / 255,
        80 + c2 * (255 - 80) / 255,
        80 + c3 * (255 - 80) / 255,
        255)
}
