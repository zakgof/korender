import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.min

class Physics(private val hf: RgImageHeightField, initialPosition: Vec3) {


    val throttleDirection = -1.z
    var orientation: Quaternion = Quaternion.IDENTITY
    var position: Vec3 = initialPosition

    var velocity: Vec3 = Vec3.ZERO
    var omega: Vec3 = Vec3.ZERO

    var throttle: Float = 0f
    var brake: Float = 0f
    var steer: Float = 0f

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
                force -= velocity.normalize() * ( reaction * 0.5f) + velocity * (brake * 10.0f + 0.3f)
            } else if (force.lengthSquared() > 1e-4){
                force -= force.normalize() * min(force.length(), reaction * 0.5f)
            }
        }
        velocity += force * dt
        position += velocity * dt
        println("cp1 $orientation")
        orientation = Quaternion.fromAxisAngle(1.y, -steer * 0.3f * dt) * orientation
        println("cp2 $orientation")
        if (deep > 0) {
            val bugNormal = orientation * 1.y
            orientation = Quaternion.shortestArc(bugNormal, normal) * orientation
            println("cp3 $orientation $bugNormal $normal")
            position = hf.surface(position, -0.001f)
        }

        println("cp4 $orientation")
        orientation = orientation.normalize()
        println("cp5 $orientation")
        println("Deep $deep   v=$velocity  q.l=${orientation.length()} force=$force")
    }

    fun transform() = Transform().rotate(orientation).translate(position)

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
