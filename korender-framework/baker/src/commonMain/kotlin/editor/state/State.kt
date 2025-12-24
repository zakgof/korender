package editor.state

import com.zakgof.korender.math.Vec3
import editor.model.Brush

data class State(
    val mouseMode: MouseMode = MouseMode.CREATOR,
    val viewCenter: Vec3 = Vec3.ZERO,
    val projectionScale: Float = 1f,        // pixel per world unit
    val gridScale: Float = 16f,             // world units

    val creatorBrush: Brush = Brush(Vec3(-128f, -128f, -64f), Vec3(128f, 128f, 64f))
) {
    enum class MouseMode {
        CREATOR,
        SELECT,
        DRAG
    }
}