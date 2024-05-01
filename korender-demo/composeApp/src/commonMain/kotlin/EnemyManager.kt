
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import kotlin.math.sin


class EnemyManager(private val hf: HeightField) {

    private val headsCount = 1 //  TODO 10
    val heads = mutableListOf<Head>()

    fun update(characterPosition: Vec3, time: Float, dt: Float) {
        if (heads.size < headsCount) {
            heads.add(Head(characterPosition))
        }
        heads.forEach { it.update(characterPosition, dt, hf) }
    }

    fun hit(head: EnemyManager.Head) {
        heads.remove(head)
    }

    class Head(characterPosition: Vec3) {

        private var position: Vec3 = characterPosition + Vec3.random().normalize() * 120.0f
        private var velocity: Vec3 = Vec3.ZERO

        fun update(characterPosition: Vec3, dt: Float, hf: HeightField) {
            val diff = characterPosition - position
            val mainDir = diff.normalize()
            val altDir = (mainDir % hf.normal(position.x, position.z)).normalize()
            velocity = mainDir * 2.0f + altDir * 0.3f  * sin(diff.length())
            position += velocity * dt
            position = hf.surface(position)
        }

        fun transform(): Transform {
            val orientation = Quaternion.shortestArc(1.z, velocity.normalize())
            return Transform().rotate(orientation).translate(position)
        }


    }

}
