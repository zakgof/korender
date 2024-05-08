import com.zakgof.korender.math.Vec3

class ExplosionManager {

    val explosions = mutableListOf<Explosion>()

    fun update(time: Float, dt: Float) {
        explosions.removeIf { it.update(time) > 1f }
    }

    class Explosion(val position: Vec3, val radius: Float, val startTime: Float) {
        var phase: Float = 0f

        fun update(time: Float): Float {
            phase = time - startTime
            return phase
        }
    }

    fun boom(position: Vec3, radius: Float, time: Float) {
        explosions.add(Explosion(position, radius, time))
    }


}
