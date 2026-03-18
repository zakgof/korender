package editor.state

import androidx.compose.ui.input.key.Key
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.model.BoundingBox
import editor.model.Material
import editor.model.brush.Brush

data class State(
    val mouseMode: MouseMode = MouseMode.CREATOR,
    val viewCenter: Vec3 = Vec3.ZERO,
    val projectionScale: Float,            // pixels per world unit
    val gridScale: Float,                  // world units per cell

    val selection: Set<String> = setOf(),
    val selectionMode: SelectionMode = SelectionMode.RESIZE,
    val clipboard: Set<Brush> = setOf(),
    val creator: BoundingBox,

    val camera: Camera = Camera(20.z, -1.z, 1.y),

    val materialId: String = Material.generic.id,

    val pressedKeys: Set<Key> = setOf(),

    val savePath: String? = null,
    val lastTextureDir: String? = null,
    val lastCompiledSceneModel: SceneModel? = null,
    val lastSavedModelHash: Int = 0
) {

    companion object {
        val STATE_KEYS = setOf(Key.W, Key.A, Key.S, Key.D, Key.DirectionLeft, Key.DirectionRight, Key.DirectionDown, Key.DirectionUp)
    }

    enum class MouseMode {
        CREATOR,
        SELECT,
        DRAG
    }

    enum class SelectionMode {
        RESIZE,
        ROTATE,
        /* SHEAR */
    }

    data class Camera(
        val position: Vec3,
        val direction: Vec3,
        val up: Vec3,
    ) {
        fun forward(dt: Float): Camera = copy(position = position + direction * dt)
        fun strafeRight(dt: Float): Camera = copy(position = position + direction.cross(up) * dt)
        fun right(dt: Float): Camera = copy(direction = (Quaternion.fromAxisAngle(up, -5f * dt) * direction).normalize())
        fun up(dt: Float): Camera {
            val q = Quaternion.fromAxisAngle(direction.cross(up), 5f * dt)
            return copy(
                direction = (q * direction).normalize(),
                up = (q * up).normalize()
            )
        }
    }
}