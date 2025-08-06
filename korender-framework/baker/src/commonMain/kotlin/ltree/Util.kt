package ltree

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

fun Vec3.randomOrtho(): Vec3 {
    val reference = if (abs(this.x) < 0.99) 1.x else 1.y
    val ortho1 = (this % reference).normalize()
    val ortho2 = (this % ortho1).normalize()
    val angle = Random().nextFloat(2f * PI)
    return ortho1 * cos(angle) + ortho2 * sin(angle)
}

fun jitter() : Float {
    return Random().nextFloat(0.9f, 1.1f)
}