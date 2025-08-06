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
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.scale
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
import tree.saveImage
import kotlin.math.abs
import kotlin.math.max

@Composable
fun LTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val lTreeDef = LTreeDef(
        DiagonalLeaves()
    )

    val lTree = generateLTree(lTreeDef)

    val lClusteredTree = clusterizeTree(lTree)

    val cards = lClusteredTree.clusters.mapIndexed{ index, cluster  ->
        captureCard(cluster, index)
    }


    Frame {
        this.background = ColorRGBA.Transparent
        AmbientLight(white(0.5f))
        DirectionalLight(Vec3(3f, 0f, 1f), white(1.0f))
        camera = camera(4.y + (-80).z, 1.z, 1.y)

        renderLTree(lTree, "genuine", 10.x)
        renderCards(cards)


        return@Frame

        val card = cards[0]
        val cluster = lClusteredTree.clusters[0]
        val right = (cluster.plane.normal % 1.y).normalize()
        val up = (right % cluster.plane.normal).normalize()

        camera = camera(card.center - card.normal * 200f, card.normal, up)
        projection = projection(card.size, card.size, 100f, 300f, ortho())
        renderLTree(cluster.lTree, "tree1")
    }
}


private fun FrameContext.renderCards(cards: List<Card>) {
    cards.forEachIndexed { index, card ->
        Renderable (
            base(colorTexture = texture("card$index", card.image)),
            mesh = biQuad(card.size, card.size),
            transform = rotate(card.normal, card.up).translate(card.center)
                .translate(-10.x)
        )
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

    saveImage(image, "png", "D:/p/test$index.png")

    return Card(
        center = cluster.plane.center,
        normal = cluster.plane.normal,
        up = up,
        size = size,
        image = image
    )
}

fun FrameContext.renderLTree(lTree: LTree, postfix: String, translation: Vec3 = 0.x) {
    if (lTree.branches.isNotEmpty()) {
        Renderable(
            base(color = ColorRGBA.Blue),
            mesh = cylinderSide(),
            instancing = instancing("trunk$postfix", lTree.branches.size, dynamic = true) {
                lTree.branches.map { branch ->
                    scale(branch.raidusAtHead, (branch.tail - branch.head).length() * 1.05f, branch.raidusAtHead)
                        .rotate(Quaternion.shortestArc(1.y, (branch.tail - branch.head).normalize()))
                        .translate(branch.head)
                        .translate(translation)
   //                   .rotate(1.y, frameInfo.time * 0.1f)
                }.forEach { Instance(it) }
            }
        )
    }
    Renderable(
        base(colorTexture = texture("model/leaf.png")),
        mesh = quad(),
        instancing = instancing("leaves$postfix", lTree.leaves.size * 2, dynamic = true) {
            lTree.leaves.flatMap { leaf ->
                listOf(-1f, 1f).map { mult ->
                    translate(0.5f.y)
                        .scale(0.16f, 0.88f, 1.0f)
                        .rotate(leaf.normal * mult, leaf.blade.normalize())
                        .translate(leaf.mount)
                        .translate(translation)
      //                .rotate(1.y, frameInfo.time * 0.1f)
                }
            }.forEach { Instance(it) }
        }
    )

}

class Card (
    val center: Vec3,
    val normal: Vec3,
    val up: Vec3,
    val size: Float,
    val image: Image
)