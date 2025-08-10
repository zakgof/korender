package com.zakgof.korender.examples.island

import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.random.Random

class Branch(
    val head: Vec3,
    val tail: Vec3,
    var radiusAtHead: Float,
    var radiusAtTail: Float,
)

fun loadBranches(bytes: ByteArray): List<Branch> =
    loadBinary(bytes) {
        val length = getInt()
        (0 until length).map {
            val head = getVec3()
            val tail = getVec3()
            val radiusAtHead = getFloat()
            val radiusAtTail = getFloat()
            Branch(head, tail, radiusAtHead, radiusAtTail)
        }
    }

fun FrameContext.renderTrunkForest(branches: List<Branch>, seeds: List<Vec3>) {

    fun thinDown(r: Float, threshold: Float): Float =
        if (r < 2f * threshold)
            (r - threshold) * r / threshold
        else
            r

    Renderable(
        base(color = ColorRGBA(0x553311FF)),
        pipe(),
        mesh = pipeMesh("trunk-forest", branches.size * seeds.size, true) {
            val r = Random(1)
            seeds.forEach { seed ->
                val transform =
                        scale(50.0f)
                            .rotate(1.y, r.nextFloat() * 2f * PI)
                            .translate(seed)
                val threshold = (seed - camera.position).length() * 3e-4f
                branches.forEach { branch ->
                    if (branch.radiusAtHead * 50.0f > threshold) {
                        sequence {
                            node(transform * branch.head, thinDown(branch.radiusAtHead * 50.0f, threshold))
                            node(transform * branch.tail, thinDown(branch.radiusAtTail * 50.0f, threshold))
                        }
                    }
                }
            }
        }
    )
}