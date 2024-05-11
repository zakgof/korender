import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.sin

class SkullManager {

    val skulls = listOf(
        Skull(Vec3(-2f, 0f, -25f)),
        Skull(Vec3(5f, 0f, 50f)),
        Skull(Vec3(15f, 0f, 10f))
    )

    fun update(characterPosition: Vec3, time: Float, dt: Float) {
        skulls.filter { !it.destroyed }
            .forEach { it.update(characterPosition, time) }
    }

    class Skull(val position: Vec3) {
        var destroyed = false
        var look = Vec3.ZERO
        fun update(characterPosition: Vec3, time: Float) {
            val diff = position - characterPosition
            val main = Vec3(diff.x, 0f, diff.z).normalize()
            val alt = main % 1.y
            look = (main + alt * 0.4f * sin(time * 10.0f + 0.001f * position.hashCode().toFloat())).normalize()
        }
    }
}
