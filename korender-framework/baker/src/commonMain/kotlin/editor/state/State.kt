package editor.state

import androidx.compose.ui.input.key.Key
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.model.Brush

data class State(
    val mouseMode: MouseMode = MouseMode.CREATOR,
    val viewCenter: Vec3 = Vec3.ZERO,
    val projectionScale: Float = 1f,        // pixel per world unit
    val gridScale: Float = 16f,             // world units

    val selectedBrush: Brush? = null,
    val creatorBrush: Brush,

    val camera: Camera = Camera(-200.z + 2.y, 1.z, 1.y),

    val pressedKeys: Set<Key> = setOf(),

    ) {

    companion object {
        val STATE_KEYS = setOf(Key.W, Key.A, Key.S, Key.D)
    }

    enum class MouseMode {
        CREATOR,
        SELECT,
        DRAG
    }

    data class Camera(
        val position: Vec3,
        val direction: Vec3,
        val up: Vec3,
    ) {
        fun forward(dt: Float): Camera = copy(position = position + direction * 100f * dt)
        fun right(dt: Float): Camera = copy(direction = (Quaternion.fromAxisAngle(up, 100f * dt) * direction).normalize())
    }
}