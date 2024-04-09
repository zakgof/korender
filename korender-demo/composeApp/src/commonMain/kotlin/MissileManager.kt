import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

class MissileManager(private val hf: HeightField) {

    private val missiles = mutableListOf<Missile>()
    private var lastTime = Float.MIN_VALUE
    fun update(dt: Float) {
        missiles.forEach { it.update(dt) }
        missiles.removeIf {
            it.position.y < hf.elevation(it.position.x, it.position.z) - 2.0f
        }
    }

    fun fire(time: Float, touchEvent: TouchEvent, transform: Transform) {
        if ((time - lastTime > 1) && (touchEvent.type == TouchEvent.Type.DOWN)) {
            lastTime = time
            missiles.add(Missile(transform))
        }
    }

    fun missiles(): List<Transform> = missiles.map { it.transform() }

    class Missile(transform: Transform) {

        private var velocity: Vec3 = 1.z + 10.y  // TODO extract orientation
        internal var position: Vec3 = transform.mat4() * Vec3(0f, 0f, 0f)
        fun update(dt: Float) {
            velocity += -5.y * dt
            position += velocity * dt
        }

        fun transform(): Transform {
            val orientation = Quaternion.shortestArc(1.z, velocity.normalize())
            return Transform().rotate(orientation).translate(position)
        }
    }
}
