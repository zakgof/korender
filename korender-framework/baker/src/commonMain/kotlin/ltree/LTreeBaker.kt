package ltree

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun LTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val lTreeDef = LTreeDef {
        sqrt(it.x * it.x + it.z * it.z) - (it.y * 0.1f).pow(0.5f) * (1.0f - it.y * 0.1f) * 10f
    }

    val lTree = generateLTree(lTreeDef)

    Frame {
        AmbientLight(white(0.3f))
        DirectionalLight(Vec3(3f, 0f, 1f), white(1.0f))
        camera = camera(4.y + (-30).z, 1.z, 1.y)
        renderLTree(lTree)
    }
}

fun FrameContext.renderLTree(lTree: LTree) {
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
    Renderable(
        base(colorTexture = texture("model/leaf.png")),
        mesh = quad(0.05f, 0.14f),
        instancing = instancing("leaves", lTree.leafs.size * 2, dynamic = true) {
            lTree.leafs.map { leaf ->
                translate(0.1f.y)
                    .rotate(leaf.normal, leaf.bladeDir)
                    .translate(leaf.mount)
                    .rotate(1.y, frameInfo.time * 0.1f)

            }.forEach { Instance(it) }
            lTree.leafs.map { leaf ->
                translate(-0.1f.y)
                    .rotate(1.y, PI)
                    .rotate(leaf.normal, -leaf.bladeDir)
                    .translate(leaf.mount)
                    .rotate(1.y, frameInfo.time * 0.1f)

            }.forEach { Instance(it) }
        }
    )
    return
    Renderable(
        base(color = ColorRGBA.Green),
        mesh = sphere(0.1f),
        instancing = instancing("attr", lTree.attractors.size, dynamic = true) {
            lTree.attractors.forEach {
                Instance(
                    translate(it)
                        .rotate(1.y, frameInfo.time * 0.1f)
                )
            }
        }
    )
}