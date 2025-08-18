package ltree.generator

import com.zakgof.korender.math.Vec3

class LTree(
    val branches: List<Branch>,
    val leaves: List<Leaf>,
) {

    interface Branch {
        val head: Vec3
        val tail: Vec3
        var raidusAtHead: Float
        var raidusAtTail: Float
    }

    class Leaf(
        val mount: Vec3,
        val blade: Vec3,
        val normal: Vec3,
        val width: Float
    )

}



