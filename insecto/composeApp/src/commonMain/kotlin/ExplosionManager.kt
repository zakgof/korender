import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.random.Random

class ExplosionManager {

    val explosions = mutableListOf<Explosion>()
    val splinters = mutableListOf<Splinter>()

    fun update(time: Float, dt: Float) {
        explosions.removeAll { !it.update(time) }
        splinters.removeAll { !it.update(time, dt) }
    }

    fun boom(position: Vec3, startRadius: Float, finishRadius: Float, time: Float) {
        explosions.add(Explosion(position, startRadius, finishRadius, time))
        if (finishRadius > 8f) {
            for (i in 1..1000) {
                if (splinters.size < 5000) {
                    splinters.add(Splinter(position, time))
                }
            }
        }
    }

    class Explosion(val position: Vec3, val startRadius: Float, val finishRadius: Float, val startTime: Float) {
        var phase: Float = 0f

        fun update(time: Float): Boolean {
            phase = (time - startTime) * (finishRadius - startRadius) / finishRadius + startRadius / finishRadius
            return phase < 1f
        }
    }

    class Splinter(initialPosition: Vec3, val startTime: Float) {

        var orientation = Quaternion.IDENTITY
        var position = initialPosition
        private var velocity = Vec3.random() * (Random.nextFloat() * 4.0f + 3.0f)
        private val axis = Vec3.random()
        private val eol = startTime + 5f + Random.nextFloat() * 2.0f
        fun update(time: Float, dt: Float): Boolean {
            orientation = Quaternion.fromAxisAngle(axis, Random.nextFloat() * 10.0f * dt) * orientation
            velocity += -5.y * dt
            position += velocity * dt
            return time < eol
        }
    }


}
