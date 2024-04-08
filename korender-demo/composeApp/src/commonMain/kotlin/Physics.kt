
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

class Physics(private val hf: RgImageHeightField, initialPosition: Vec3) {


    val throttleDirection = -1.z
    var orientation: Quaternion = Quaternion.IDENTITY
    var position: Vec3 = initialPosition

    var velocity: Vec3 = Vec3.ZERO
    var omega: Vec3 = Vec3.ZERO

    var throttle: Float = 0f
    var brake: Float = 0f
    var steer: Float = 0f

    fun update(dt: Float): Transform {

        val force = orientation * (throttleDirection * throttle * 10.0f) - 5.y - velocity * (brake * 2.0f + 0.3f)
        velocity += force * dt
        position += velocity * dt

        orientation = Quaternion.fromAxisAngle(1.y, -steer * 0.3f * dt) * orientation

        val surfaceY = hf.elevation(position.x, position.z)
        val deep = surfaceY - position.y

        // println("Velocity: $velocity Deep $deep")

        if (deep > 0) {
            val normal = hf.normal(position.x, position.z)
            velocity -= normal * (velocity * normal)
            position += normal * ((deep + 0.001f) * normal.y)
        }

        return Transform().rotate(orientation).translate(position)
    }

    fun forward(touch: TouchEvent) {
        if (touch.type == TouchEvent.Type.DOWN) {
            throttle = 1f
            brake = 0f
        }
        if (touch.type == TouchEvent.Type.UP) {
            throttle = 0f
        }
    }

    fun backward(touch: TouchEvent) {
        if (touch.type == TouchEvent.Type.DOWN) {
            brake = 1f
            throttle = 0f
        }
        if (touch.type == TouchEvent.Type.UP) {
            brake = 0f
        }
    }

    fun left(touch: TouchEvent) {
        if (touch.type == TouchEvent.Type.DOWN) {
            steer = -1f
        }
        if (touch.type == TouchEvent.Type.UP) {
            steer = 0f
        }
    }

    fun right(touch: TouchEvent) {
        if (touch.type == TouchEvent.Type.DOWN) {
            steer = 1f
        }
        if (touch.type == TouchEvent.Type.UP) {
            steer = 0f
        }
    }


}
