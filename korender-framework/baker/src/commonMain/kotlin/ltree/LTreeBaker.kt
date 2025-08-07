package ltree

import androidx.compose.runtime.Composable
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.SCALE
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
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
import tree.saveImage
import kotlin.math.abs
import kotlin.math.max


@Composable
fun LTreeBaker2() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    Frame {
        AmbientLight(white(0.05f))
        DirectionalLight(-1.z)
        camera = camera(20.z + 3.y, -1.z, 1.y)
        Renderable(
            base(color = ColorRGBA.Green),
            pipe(),
            mesh = customMesh("pipe", 8, 12, POS, NORMAL, TEX, SCALE) {
                pos(0.y).normal(3.y + 1.x).tex(0f, 0f).attr(SCALE, 0.2f, 0.4f)
                pos(0.y).normal(3.y + 1.x).tex(1f, 0f).attr(SCALE, 0.2f, 0.4f)
                pos(0.y).normal(3.y + 1.x).tex(1f, 1f).attr(SCALE, 0.2f, 0.4f)
                pos(0.y).normal(3.y + 1.x).tex(0f, 1f).attr(SCALE, 0.2f, 0.4f)
                pos(3.y + 1.x).normal(2.y - 1.x).tex(0f, 0f).attr(SCALE, 0.4f, 0.3f)
                pos(3.y + 1.x).normal(2.y - 1.x).tex(1f, 0f).attr(SCALE, 0.4f, 0.3f)
                pos(3.y + 1.x).normal(2.y - 1.x).tex(1f, 1f).attr(SCALE, 0.4f, 0.3f)
                pos(3.y + 1.x).normal(2.y - 1.x).tex(0f, 1f).attr(SCALE, 0.4f, 0.3f)
                index(0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7)
            }
        )
        Renderable(
            base(color = ColorRGBA.Green),
            mesh = sphere(0.05f),
            transform = translate(3.y + 1.x)
        )
        Renderable(
            base(color = ColorRGBA.Green),
            mesh = sphere(0.05f),
            transform = translate(0.x)
        )
    }
}

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
        AmbientLight(white(0.5f))
        DirectionalLight(Vec3(3f, 0f, 1f), white(1.0f))
        camera = camera(4.y + (-80).z, 1.z, 1.y)

        renderLTree(lTree, "genuine", 10.x)
        renderCards(cards)

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

    Renderable(
        base(color = ColorRGBA.Blue),
        pipe(),
        mesh = customMesh("trunk$postfix", 4 * lTree.branches.size, 6 * lTree.branches.size, POS, NORMAL, TEX, SCALE) {
            lTree.branches.mapIndexed { i, branch ->
                val len = (branch.tail - branch.head) * 1.05f
                pos(branch.head).normal(len).tex(0f, 0f).attr(SCALE, branch.raidusAtHead * 3f, branch.raidusAtTail * 3f)
                pos(branch.head).normal(len).tex(1f, 0f).attr(SCALE, branch.raidusAtHead * 3f, branch.raidusAtTail * 3f)
                pos(branch.head).normal(len).tex(1f, 1f).attr(SCALE, branch.raidusAtHead * 3f, branch.raidusAtTail * 3f)
                pos(branch.head).normal(len).tex(0f, 1f).attr(SCALE, branch.raidusAtHead * 3f, branch.raidusAtTail * 3f)
                index(i * 4 + 0, i * 4 + 1, i * 4 + 2, i * 4 + 0, i * 4 + 2, i * 4 + 3)
            }
        },
        transform =
            rotate(1.y, frameInfo.time * 0.1f)
                .translate(translation)
    )

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

private fun FrameContext.renderCards(cards: List<Card>) {
    cards.forEachIndexed { index, card ->
        Renderable(
            base(colorTexture = texture("card$index", card.image)),
            mesh = biQuad(card.size, card.size),
            transform = rotate(card.normal, card.up).translate(card.center)
                .translate(-10.x)
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