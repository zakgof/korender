package ltree

import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class LTree(
    val branches: List<Branch>,
    val attractors: List<Vec3>
) {

    class Branch(
        val head: Vec3,
        val tail: Vec3
    )
}

fun generateLTree(lTreeDef: LTreeDef): LTree {
    val branches = mutableListOf<LTree.Branch>()
    val attractors = initializeAttractors(lTreeDef)

    fun rayMetric(point: Vec3, look: Vec3): Float {
        val dirPower = attractors.sumOf { 1.0 / distancePointToRay(it, point, look) }.toFloat() / attractors.size
        val dirSymmetry = attractors.fold(Vec3.ZERO) { a, it ->
            (it - point).normalize() * (1.0f / distancePointToRay(it, point, look))
        }

        val metric = dirPower // + 0.03f * (dirSymmetry * look)

        println("Look: $look | dirPower: $dirPower | dirSymmetry: $dirSymmetry | metric: $metric")
        return metric
    }

    fun step(point: Vec3, look: Vec3, level: Int) {
        val tail = point + look
        val branch = LTree.Branch(point, tail)
        branches += branch

        if (level == 12)
            return

        val ortho = (0 until 32)
            .map { look.randomOrtho() }
            .maxBy { o ->
                rayMetric(point, (look + o * 0.5f).normalize()) +
                        rayMetric(point, (look - o * 0.5f).normalize())
            }

        val l1 = (look + ortho * 0.5f).normalize()
        val l2 = (look - ortho * 0.5f).normalize()

        val rm1 = rayMetric(point + l1 * 1.0f, l1)
        val rm2 = rayMetric(point + l2 * 1.0f, l2)

        if (rm1 < 0.1f || rm2 < 0.1f)
            return

        step(tail, l1, level + 1)
        step(tail, l2, level + 1)
    }

    step(-1.y, 1.y, 0)
    return LTree(branches, attractors)
}

fun initializeAttractors(lTreeDef: LTreeDef) =
    grid(
        -10f to 10f,
        0f to 20f,
        -10f to 10f,
        64
    )
        .filter {
            abs(lTreeDef.sdf(it)) < 0.1f
        }.toMutableList()

private fun grid(xRange: Pair<Float, Float>, yRange: Pair<Float, Float>, zRange: Pair<Float, Float>, steps: Int): List<Vec3> {
    val r = Random()
    return (0..steps).flatMap { xx ->
        (0..steps).flatMap { yy ->
            (0..steps).map { zz ->
                Vec3(
                    xRange.first + (xx + r.nextFloat(0.4f)) * (xRange.second - xRange.first) / steps,
                    yRange.first + (yy + r.nextFloat(0.4f)) * (yRange.second - yRange.first) / steps,
                    zRange.first + (zz + r.nextFloat(0.4f)) * (zRange.second - zRange.first) / steps
                )
            }
        }
    }
}

private fun Vec3.randomOrtho(): Vec3 {
    val reference = if (abs(this.x) < 0.99) 1.x else 1.y
    val ortho1 = (this % reference).normalize()
    val ortho2 = (this % ortho1).normalize()
    val angle = Random().nextFloat(2f * PI)
    return ortho1 * cos(angle) + ortho2 * sin(angle)
}

private fun distancePointToRay(r: Vec3, origin: Vec3, look: Vec3): Float {
    val lNorm = look.normalize()
    val op = r - origin
    val t = op * lNorm
    return if (t < 0f) {
        1000f
    } else {
        val closest = origin + lNorm * t
        (r - closest).length()
    }
}



