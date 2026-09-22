package editor.korender

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

/**
 * Drag-to-rotate state for a Korender preview.
 *
 * Apply to any Korender view:
 * ```
 * val orbit = rememberOrbitRotation(modelId, scaleX, scaleY, scaleZ)
 * Korender {
 *     OnTouch { orbit.handleTouch(it) }
 *     Frame {
 *         Model(res, transform = orbit.modelTransform(scale, pivot))
 *     }
 * }
 * ```
 * Passing reset keys to [rememberOrbitRotation] recreates the state (resetting
 * yaw/pitch) whenever e.g. the displayed model or its scale changes.
 */
class OrbitRotationState(
    val sensitivity: Float = 0.01f,
    val maxPitch: Float = 1.45f
) {
    var yaw by mutableStateOf(0f)
        private set
    var pitch by mutableStateOf(0f)
        private set

    private var lastX: Float? = null
    private var lastY: Float? = null
    private var dragging = false

    fun handleTouch(event: TouchEvent) {
        when (event.type) {
            TouchEvent.Type.DOWN -> {
                if (event.button == TouchEvent.Button.LEFT) {
                    dragging = true
                    lastX = event.x
                    lastY = event.y
                }
            }
            TouchEvent.Type.MOVE -> {
                if (dragging) {
                    val px = lastX
                    val py = lastY
                    if (px != null && py != null) {
                        yaw += (event.x - px) * sensitivity
                        pitch = (pitch + (event.y - py) * sensitivity).coerceIn(-maxPitch, maxPitch)
                    }
                    lastX = event.x
                    lastY = event.y
                }
            }
            TouchEvent.Type.UP -> {
                dragging = false
                lastX = null
                lastY = null
            }
        }
    }

    fun reset() {
        yaw = 0f
        pitch = 0f
        lastX = null
        lastY = null
        dragging = false
    }

    fun rotation(): Quaternion =
        Quaternion.fromAxisAngle(Vec3.Y, yaw) * Quaternion.fromAxisAngle(Vec3.X, pitch)

    /**
     * Model transform that applies [scale] about the origin and then the
     * user rotation about [pivot] (pass the scaled bounding-sphere center
     * to keep the object centered while rotating, preserving best-fit framing).
     */
    fun modelTransform(scale: Vec3, pivot: Vec3): Transform =
        Transform.translate(pivot) * Transform.rotate(rotation()) *
            Transform.translate(-pivot) * Transform.scale(scale)
}

@Composable
fun rememberOrbitRotation(
    vararg resetKeys: Any?,
    sensitivity: Float = 0.01f
): OrbitRotationState {
    // NOTE: the instance must stay stable across recompositions. Korender's
    // setup block (and hence OnTouch/Frame lambdas) is registered once at
    // Engine init, so recreating the state on key change would leave Frame
    // holding a stale instance. Reset the same instance instead.
    val orbit = remember { OrbitRotationState(sensitivity) }
    LaunchedEffect(*resetKeys) { orbit.reset() }
    return orbit
}
