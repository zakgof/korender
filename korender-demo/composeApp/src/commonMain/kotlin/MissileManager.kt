
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

class MissileManager(private val hf: HeightField, private val explosionManager: ExplosionManager) {

    val missiles = mutableListOf<Missile>()

    private var lastFireTime = Float.MIN_VALUE

    fun update(time: Float, dt: Float) {
        missiles.forEach { it.update(dt) }
        missiles.removeIf {
            if (it.position.y < hf.elevation(it.position.x, it.position.z)  - 0.1f) {
                explosionManager.boom(hf.surface(it.position), 12f, time)
                true
            } else {
                false
            }
        }
    }

    fun missileHitEnemy(missile: Missile, time: Float) {
        missiles.remove(missile)
        explosionManager.boom(missile.position, 24f, time)
    }

    fun fire(time: Float, touchEvent: TouchEvent, transform: Transform, launcherVelocity: Vec3) {
        if (canFire(time) && (touchEvent.type == TouchEvent.Type.DOWN)) {
            lastFireTime = time
            missiles.add(Missile(transform, launcherVelocity))
        }
    }

    fun canFire(time: Float) = (time - lastFireTime > 1)

    class Missile(transform: Transform, launcherVelocity: Vec3) {

        private var velocity: Vec3 = transform.applyToDirection(Vec3(0f, 6f, -30f)) + launcherVelocity
        var position: Vec3 = transform.mat4() * Vec3(0f, 1f, 0f)
        fun update(dt: Float) {
            velocity += -5.y * dt
            position += velocity * dt
        }

        fun transform(): Transform = Transform().rotate(-velocity.normalize(), 1.y).translate(position)
    }

}
