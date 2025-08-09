package ltree

import androidx.compose.runtime.Composable
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import ltree.clusterizer.ClusteredTree
import ltree.clusterizer.clusterizeTree
import ltree.generator.LTree
import ltree.generator.LTreeDef
import ltree.generator.generateLTree
import ltree.generator.leaf.DiagonalLeaves
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

@Composable
fun LTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val lTreeDef = LTreeDef(
        DiagonalLeaves()
    )

    val lTree = generateLTree(lTreeDef)

    val lClusteredTree = clusterizeTree(lTree)

    val cards = lClusteredTree.clusters.mapIndexed { index, cluster ->
        captureCard(cluster, index)
    }

    Frame {
        this.background = ColorRGBA.Transparent
        AmbientLight(white(0.8f))
        DirectionalLight(Vec3(3f, 0f, 1f), white(1.0f))
        projection = projection(5f * width / height, 5f, 5f, 2000f)
        camera = camera(20.y + (-20 + frameInfo.time * 4f).z, 1.z - 0.2f.y, 1.y + 0.2f.z)

        // renderLTree(lTree, "genuine", 10.x)
        renderTrunkForest(lTree)
        renderCardForest(cards)

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun KorenderContext.captureCard(cluster: ClusteredTree.Cluster, index: Int): Card {
    val right = (cluster.plane.normal % 1.y).normalize()
    val up = (right % cluster.plane.normal).normalize()

    val pts = cluster.lTree.leaves.map { leaf ->
        val projected = leaf.mount - cluster.plane.normal * ((leaf.mount - cluster.plane.center) * cluster.plane.normal) - cluster.plane.center
        Vec3(projected * right, projected * up, projected * cluster.plane.normal)
    }
    val size = max(
        pts.maxOf { abs(it.x) },
        pts.maxOf { abs(it.y) }
    ) * 1.1f
    val camera = camera(cluster.plane.center - cluster.plane.normal * 200f, cluster.plane.normal, up)
    val projection = projection(size, size, 100f, 300f, ortho())
    val image = this.captureFrame(1024, 1024, camera, projection) {
        AmbientLight(White)
        renderLTree(cluster.lTree, "$index")
    }

    // saveImage(image, "png", "D:/kot/dev/test$index.png")

    return Card(
        center = cluster.plane.center,
        normal = cluster.plane.normal,
        up = up,
        size = size,
        image = image
    )
}

fun FrameContext.renderLTree(lTree: LTree, postfix: String, translation: Vec3 = 0.x) {
    renderTrunk(lTree, postfix, translation)
    renderFoliage(postfix, lTree, translation)
}

private fun FrameContext.renderFoliage(postfix: String, lTree: LTree, translation: Vec3) {
    Renderable(
        base(colorTexture = texture("model/leaf.png")),
        mesh = biQuad(),
        instancing = instancing("leaves$postfix", lTree.leaves.size, dynamic = true) {
            lTree.leaves.map { leaf ->
                translate(0.5f.y)
                    .scale(0.16f, 0.88f, 1.0f)
                    .rotate(leaf.normal, leaf.blade.normalize())
                    .translate(leaf.mount)
                    .rotate(1.y, frameInfo.time * 0.1f)
                    .translate(translation)
            }.forEach { Instance(it) }
        }
    )
}

private fun FrameContext.renderTrunk(lTree: LTree, postfix: String, translation: Vec3) {
    if (lTree.branches.isNotEmpty()) {
        Renderable(
            base(color = ColorRGBA.Blue),
            pipe(),
            mesh = pipeMesh("trunk$postfix", lTree.branches.size) {
                lTree.branches.forEach { branch ->
                    sequence {
                        node(branch.head, branch.raidusAtHead)
                        node(branch.tail, branch.raidusAtTail)
                    }
                }
            },
            transform =
                rotate(1.y, frameInfo.time * 0.1f)
                    .translate(translation)
        )
    }
}

private fun FrameContext.renderCardForest(cards: List<Card>) {
    cards.forEachIndexed { index, card ->
        val r = Random(1)
        Renderable(
            base(colorTexture = texture("card$index", card.image)),
            mesh = biQuad(card.size, card.size),
            instancing = instancing("card$index", 41 * 41, false) {
                for (xx in -20..20) {
                    for (zz in 0..40) {
                        Instance(
                            rotate(card.normal, card.up)
                                .translate(card.center)
                                .rotate(1.y, r.nextFloat() * 2f * PI)
                                .translate((xx * 16f).x + (zz * 16f).z)
                        )
                    }
                }
            }
        )
    }
}

class Card(
    val center: Vec3,
    val normal: Vec3,
    val up: Vec3,
    val size: Float,
    val image: Image
)

private fun FrameContext.renderTrunkForestRef(lTree: LTree) {
    Renderable(
        base(color = ColorRGBA.Blue),
        pipe(),
        mesh = pipeMesh("trunk-forest", lTree.branches.size) {
            lTree.branches.forEach { branch ->
                sequence {
                    node(branch.head, branch.raidusAtHead)
                    node(branch.tail, branch.raidusAtTail)
                }
            }
        },
        instancing = instancing("trunk-forest", 41 * 41, false) {
            for (xx in -20..20) {
                for (zz in 0..40) {
                    Instance(translate((xx * 16f).x + (zz * 16f).z))
                }
            }
        }
    )
}

private fun FrameContext.renderTrunkForest(lTree: LTree) {

    fun thinDown(r: Float, threshold: Float): Float =
        if (r < 2f * threshold)
            (r - threshold) * r / threshold
        else
            r

    Renderable(
        base(color = ColorRGBA(0x553311FF)),
        pipe(),
        mesh = pipeMesh("trunk-forest", lTree.branches.size * 41 * 41, true) {
            val r = Random(1)
            for (xx in -20..20) {
                for (zz in 0..40) {
                    val transform = rotate(1.y, r.nextFloat() * 2f * PI)
                        .translate((xx * 16f).x + (zz * 16f).z)
                    val threshold = (transform.offset() - camera.position).length() * 5e-4f
                    lTree.branches.forEach { branch ->
                        if (branch.raidusAtHead > threshold) {
                            sequence {
                                node(transform * branch.head, thinDown(branch.raidusAtHead, threshold))
                                node(transform * branch.tail, thinDown(branch.raidusAtTail, threshold))
                            }
                        }
                    }
                }
            }
        }
    )
}