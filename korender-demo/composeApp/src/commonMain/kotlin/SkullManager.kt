import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.sin

class SkullManager(private val hf: HeightField) {

    val skulls = mutableListOf(
        Skull(hf, Vec3(-4542f, 0f, -4672f)),
        Skull(hf, Vec3(-4413f, 0f, -4753f)),
        Skull(hf, Vec3(-4254f, 0f, -4819f))
    )

    fun update(characterPosition: Vec3, time: Float, dt: Float) {
        skulls.filter { !it.destroyed }
            .forEach { it.update(characterPosition, time) }
    }

    fun hit(skull: Skull) {
        skull.hit()
    }

    class Skull(private val hf: HeightField, val position: Vec3) {
        var destroyed = false
        var transform = Transform()
        fun update(characterPosition: Vec3, time: Float) {
            val diff = position - characterPosition
            val main = Vec3(diff.x, 0f, diff.z).normalize()
            val alt = main % 1.y
            val look = (main + alt * 0.4f * sin(time * 10.0f + 0.001f * position.hashCode().toFloat())).normalize()
            transform = Transform().rotate(look, 1.y).scale(2.0f).translate(hf.surface(position, 9.5f))
        }

        fun hit() {
            destroyed = true
        }
    }
}
