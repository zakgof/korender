package ltree

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun Vec3.randomOrtho(r: Random): Vec3 {
    val reference = if (abs(this.x) < 0.99) 1.x else 1.y
    val ortho1 = (this % reference).normalize()
    val ortho2 = (this % ortho1).normalize()
    val angle = r.nextFloat() * 2f * PI
    return ortho1 * cos(angle) + ortho2 * sin(angle)
}

fun Vec3.randomOrtho() = randomOrtho(Random)

fun Random.floatIn(f1: Float, f2: Float) = f1 + this.nextFloat() * (f2 - f1)

fun jitter() : Float {
    return Random.nextFloat() * 0.2f + 0.9f
}