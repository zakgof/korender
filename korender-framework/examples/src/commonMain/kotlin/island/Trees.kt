package com.zakgof.korender.examples.island

import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import kotlin.random.Random

class Branch(
    val head: Vec3,
    val tail: Vec3,
    var radiusAtHead: Float,
    var radiusAtTail: Float,
)

class Card(
    val center: Vec3,
    val normal: Vec3,
    val up: Vec3,
    val size: Float,
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

fun loadCards(bytes: ByteArray): List<Card> =
    loadBinary(bytes) {
        val length = getInt()
        (0 until length).map {
            val center = getVec3()
            val normal = getVec3()
            val up = getVec3()
            val size = getFloat()
            Card(center, normal, up, size)
        }
    }

fun FrameContext.renderTrees(branches: List<Branch>, cards: List<Card>, seeds: List<Vec3>) {
    renderBranches(branches, seeds)
    renderCards(cards, seeds)
}

fun FrameContext.renderBranches(branches: List<Branch>, seeds: List<Vec3>) {

    fun thinDown(r: Float, threshold: Float): Float =
        if (r < 2f * threshold) (r - threshold) * r / threshold else r

    Renderable(
        base(color = ColorRGBA(0x553311FF)),
        pipe(),
        mesh = pipeMesh("trunk-forest", branches.size * seeds.size, true) {
            val r = Random(1)
            seeds.forEach { seed ->
                val transform =
                    scale(50.0f)
                        //  .rotate(1.y, r.nextFloat() * 2f * PI)
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

fun FrameContext.renderCards(cards: List<Card>, seeds: List<Vec3>) {
    val r = Random(1)
    val scale = 50.0f
    Renderable(
        base(colorTexture = texture("island/tree/atlas.png")),
        plugin("discard", "island/tree/shader/island.foliage.discard.frag"),
        mesh = customMesh(
            "foliage", cards.size * seeds.size * 8, cards.size * seeds.size * 12,
            POS, NORMAL, TEX, MODEL0, MODEL1, MODEL2, MODEL3, dynamic = false
        ) {
            var indexBase = 0
            cards.forEachIndexed { index, card ->
                val right = card.normal % card.up
                val p1 = card.center * scale + (-card.up - right) * (scale * card.size)
                val p2 = card.center * scale + (-card.up + right) * (scale * card.size)
                val p3 = card.center * scale + (card.up + right) * (scale * card.size)
                val p4 = card.center * scale + (card.up - right) * (scale * card.size)
                val texX = 0.25f * (index % 4)
                val texY = 0.25f * (index / 4)
                seeds.forEach { seed ->
                    pos(seed + p1).normal(card.normal).tex(texX, texY)
                    pos(seed + p2).normal(card.normal).tex(texX + 0.25f, texY)
                    pos(seed + p3).normal(card.normal).tex(texX + 0.25f, texY + 0.25f)
                    pos(seed + p4).normal(card.normal).tex(texX, texY + 0.25f)
                    pos(seed + p1).normal(-card.normal).tex(texX, texY)
                    pos(seed + p2).normal(-card.normal).tex(texX + 0.25f, texY)
                    pos(seed + p3).normal(-card.normal).tex(texX + 0.25f, texY + 0.25f)
                    pos(seed + p4).normal(-card.normal).tex(texX, texY + 0.25f)
                    index(indexBase + 0, indexBase + 1, indexBase + 2, indexBase + 0, indexBase + 2, indexBase + 3)
                    index(indexBase + 4, indexBase + 6, indexBase + 5, indexBase + 4, indexBase + 7, indexBase + 6)
                    indexBase += 8
                }
            }
        })
}