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
    val branches: List<Branch>
) {

    class Branch(
        val head: Vec3,
        val tail: Vec3
    )
}

fun generateLTree(lTreeDef: LTreeDef): LTree {
    val branches = mutableListOf<LTree.Branch>()

    fun step(point: Vec3, look: Vec3, level: Int) {
        val tail = point + look
        val branch = LTree.Branch(point, tail)
        branches += branch

        if (level == 4) return

        val look1 = (look + look.randomOrtho() * 0.7f).normalize()
        val look2 = (look + look.randomOrtho() * 0.7f).normalize()

        step(tail, look1, level + 1)
        step(tail, look2, level + 1)
    }

    step(0.y, 1.y, 0)
    return LTree(branches)
}

fun Vec3.randomOrtho(): Vec3 {
    val reference = if (abs(this.x) < 0.99) 1.x else 1.y
    val ortho1 = (this % reference).normalize()
    val ortho2 = (this % ortho1).normalize()
    val angle = Random().nextFloat(2f * PI)
    return ortho1 * cos(angle) + ortho2 * sin(angle)
}



