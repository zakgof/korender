package insecto
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlin.math.sin


class EnemyManager(private val hf: HeightField) {

    private val headsCount = 10
    val heads = mutableListOf<Head>()

    fun update(characterPosition: Vec3, dt: Float) {
        if (heads.size < headsCount) {
            heads.add(Head(characterPosition))
        }
        heads.removeAll { !it.update(characterPosition, dt, hf) }
    }

    fun hit(head: Head) = heads.remove(head)

    class Head(characterPosition: Vec3) {

        private var position: Vec3 = characterPosition + Vec3.random().normalize() * 120.0f
        private var velocity: Vec3 = Vec3.ZERO
        private var transform: Transform = Transform()

        fun update(characterPosition: Vec3, dt: Float, hf: HeightField) : Boolean {
            val diff = characterPosition - position
            val mainDir = diff.normalize()
            val normal = hf.normal(position.x, position.z)
            val altDir = (mainDir % normal).normalize()
            velocity = mainDir * 2.0f + altDir * 0.6f  * sin(diff.length())
            position += velocity * dt
            position = hf.surface(position, 1.0f)
            transform = Transform().rotate(-velocity.normalize(), normal).translate(position)
            return (position - characterPosition).lengthSquared() < 200f * 200f
        }

        fun transform(): Transform = transform


    }

}
