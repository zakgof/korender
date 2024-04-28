
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

class MissileManager(private val hf: HeightField) {

    val missiles = mutableListOf<Missile>()
    private val explosions = mutableListOf<Explosion>()
    private var lastTime = Float.MIN_VALUE
    fun update(time: Float, dt: Float) {
        missiles.forEach { it.update(dt) }
        missiles.removeIf {
            if (it.position.y < hf.elevation(it.position.x, it.position.z)  - 0.1f) {
                explosions.add(Explosion(hf.surface(it.position), time))
                true
            } else {
                false
            }
        }
    }

    fun missileHitEnemy(missile: MissileManager.Missile, time: Float) {
        missiles.remove(missile)
        explosions.add(Explosion(missile.position, time))
    }

    fun fire(time: Float, touchEvent: TouchEvent, transform: Transform, launcherVelocity: Vec3) {
        if (canFire(time) && (touchEvent.type == TouchEvent.Type.DOWN)) {
            lastTime = time
            missiles.add(Missile(transform, launcherVelocity))
        }
    }

    fun canFire(time: Float) = (time - lastTime > 1)

    fun explosions(time: Float): List<Pair<Vec3, Float>> {
        explosions.removeIf { time - it.startTime > 1f}
        return explosions.map { Pair(it.position, time - it.startTime) }
    }

    class Missile(transform: Transform, launcherVelocity: Vec3) {

        private var velocity: Vec3 = transform.applyToDirection(Vec3(0f, 6f, -30f)) + launcherVelocity
        var position: Vec3 = transform.mat4() * Vec3(0f, 1f, 0f)
        fun update(dt: Float) {
            velocity += -5.y * dt
            position += velocity * dt
        }

        fun transform(): Transform {
            val orientation = Quaternion.shortestArc(1.z, velocity.normalize())
            return Transform().rotate(orientation).translate(position)
        }
    }

    class Explosion(val position: Vec3, val startTime: Float)
}
