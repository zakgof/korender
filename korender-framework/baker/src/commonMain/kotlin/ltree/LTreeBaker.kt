package ltree

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import ltree.generator.LTree
import ltree.generator.LTreeDef
import ltree.generator.generateLTree
import ltree.generator.leaf.DiagonalLeaves

@Composable
fun LTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val lTreeDef = LTreeDef(
        DiagonalLeaves()
    )

    val lTree = generateLTree(lTreeDef)

    Frame {
        AmbientLight(white(0.5f))
        DirectionalLight(Vec3(3f, 0f, 1f), white(1.0f))
        camera = camera(4.y + (-30).z, 1.z, 1.y)
        renderLTree(lTree)
    }
}

fun FrameContext.renderLTree(lTree: LTree) {
    if (true) {
        Renderable(
            base(color = ColorRGBA.Blue),
            mesh = cylinderSide(),
            instancing = instancing("trunk", lTree.branches.size, dynamic = true) {
                lTree.branches.map { branch ->
                    scale(branch.raidusAtHead, (branch.tail - branch.head).length() * 1.05f, branch.raidusAtHead)
                        .rotate(Quaternion.shortestArc(1.y, (branch.tail - branch.head).normalize()))
                        .translate(branch.head)
                        .rotate(1.y, frameInfo.time * 0.1f)
                }.forEach { Instance(it) }
            }
        )
    }
    Renderable(
        base(colorTexture = texture("model/leaf.png")),
        mesh = quad(),
        instancing = instancing("leaves", lTree.leafs.size * 2, dynamic = true) {
            lTree.leafs.flatMap { leaf ->
                listOf(-1f, 1f).map { mult ->
                    translate(0.5f.y)
                        .scale(0.16f, 0.88f, 1.0f)
                        .rotate(leaf.normal * mult, leaf.blade.normalize())
                        .translate(leaf.mount)
                        .rotate(1.y, frameInfo.time * 0.1f)
                }
            }.forEach { Instance(it) }
        }
    )

}