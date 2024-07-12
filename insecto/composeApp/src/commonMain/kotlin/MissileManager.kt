
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.cos
import kotlin.math.sin

class MissileManager(private val hf: HeightField, private val explosionManager: ExplosionManager) {

    val missiles = mutableListOf<Missile>()

    private var lastFireTime = Float.MIN_VALUE

    fun update(time: Float, dt: Float) {
        missiles.forEach { it.update(dt) }
        missiles.removeIf {
            if (it.position.y < hf.elevation(it.position.x, it.position.z)  - 0.1f) {
                explosionManager.boom(hf.surface(it.position), 0f, 6f, time)
                true
            } else {
                false
            }
        }
    }

    fun destroyMissile(missile: Missile) {
        missiles.remove(missile)
    }

    fun fire(time: Float, touchEvent: TouchEvent, transform: Transform, launcherVelocity: Vec3, cannonAngle: Float) {
        if (canFire(time) && (touchEvent.type == TouchEvent.Type.DOWN)) {
            lastFireTime = time
            missiles.add(Missile(transform, launcherVelocity, cannonAngle))
        }
    }

    fun canFire(time: Float) = (time - lastFireTime > 1)

    class Missile(transform: Transform, launcherVelocity: Vec3, cannonAngle: Float) {

        private var velocity: Vec3 = transform.applyToDirection(Vec3(0f, 50f * sin(cannonAngle), -50f * cos(cannonAngle))) + launcherVelocity
        var position: Vec3 = transform.mat4 * Vec3(0f, 1f, 0f)
        fun update(dt: Float) {
            velocity += -5.y * dt
            position += velocity * dt
        }

        fun transform(): Transform = Transform().rotate(-velocity.normalize(), 1.y).translate(position)
    }

}
