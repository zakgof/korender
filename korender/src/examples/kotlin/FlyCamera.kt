import com.zakgof.korender.Platform
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.math.FloatMath.cos
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Vec3

class FlyCamera(platform: Platform, startPos: Vec3) {

    private val rotationSpeed = 1e-9f;
    private val moveSpeed = 1e-8f
    private var position = startPos;
    private var phi = 0f
    private var theta = 0f

    private val pressedKeys = mutableSetOf<Int>()

    var camera = update()

    init {
        platform.onKey = {
            if (it.press) {
                pressedKeys.add(it.code)
            } else {
                pressedKeys.remove(it.code)
            }
        }
    }

    private fun update(): Camera {
        val look = look()
        val right = look % Vec3.Y
        val up = (right % look).normalize()
        return DefaultCamera(position, look, up)
    }

    fun idle(dt: Long) : Camera {
        if (pressedKeys.contains('A'.code))
            phi -= rotationSpeed * dt
        if (pressedKeys.contains('D'.code))
            phi += rotationSpeed * dt
        if (pressedKeys.contains(265))
            theta += rotationSpeed * dt
        if (pressedKeys.contains(264))
            theta -= rotationSpeed * dt
        if (pressedKeys.contains('W'.code))
            position += look() * (moveSpeed * dt)
        if (pressedKeys.contains('S'.code))
            position -= look() * (moveSpeed * dt)
        camera = update()
        return camera
    }

    private fun look() = Vec3(cos(theta) * sin(phi), sin(theta), -cos(theta) * cos(phi))
}
