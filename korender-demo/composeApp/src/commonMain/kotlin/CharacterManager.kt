
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.max
import kotlin.math.min


class CharacterManager(private val hf: RgImageHeightField, initialPosition: Vec3) {

    var score: Int = 0
    private val throttleDirection = -1.z
    private var orientation: Quaternion = Quaternion.IDENTITY
    private var position: Vec3 = initialPosition

    var velocity: Vec3 = Vec3.ZERO

    private var throttle: Float = 0f
    private var brake: Float = 0f
    private var steer: Float = 0f

    var cannonAngle: Float = 0.2f
    private var cannonVelocity: Float = 0f

    fun update(dt: Float) {

        val surfaceY = hf.elevation(position.x, position.z)
        val deep = surfaceY - position.y

        var force = -5.y

        val normal = hf.normal(position.x, position.z)
        if (deep > 0) {
            velocity -= normal * (velocity * normal)
            force += orientation * (throttleDirection * throttle * 10.0f)
            val reaction = -(force * normal)
            force += normal * reaction
            if (velocity.lengthSquared() > 1e-4) {
                force -= velocity.normalize() * (reaction * 0.5f) + velocity * (brake * 10.0f + 0.3f)
            } else if (force.lengthSquared() > 1e-4) {
                force -= force.normalize() * min(force.length(), reaction * 0.5f)
            }
        }
        velocity += force * dt
        position += velocity * dt

        orientation = Quaternion.fromAxisAngle(orientation * 1.y, -steer * 1.3f * dt) * orientation
        if (deep > 0) {
            val preferredDir = orientation * 1.z
            val right = preferredDir % normal
            val look = (normal % right).normalize()
            orientation = Quaternion.lookAt(look, normal)
            position = hf.surface(position, -0.001f)
        }
        orientation = orientation.normalize()

        cannonAngle += cannonVelocity * dt
        cannonAngle = max(cannonAngle, 0f)
        cannonAngle = min(cannonAngle, 0.5f)
    }

    fun transform()= Transform().rotate(orientation).translate(position)

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
            throttle = -0.5f
        }
        if (touch.type == TouchEvent.Type.UP) {
            brake = 0f
            throttle = 0f
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

    fun incrementScore(inc: Int) {
        score += inc
    }

    fun cannonUp(touch: TouchEvent) {
        if (touch.type == TouchEvent.Type.DOWN) {
            cannonVelocity = 0.8f
        }
        if (touch.type == TouchEvent.Type.UP) {
            cannonVelocity = 0f
        }
    }

    fun cannonDown(touch: TouchEvent) {
        if (touch.type == TouchEvent.Type.DOWN) {
            cannonVelocity = -0.8f
        }
        if (touch.type == TouchEvent.Type.UP) {
            cannonVelocity = 0f
        }
    }

}


