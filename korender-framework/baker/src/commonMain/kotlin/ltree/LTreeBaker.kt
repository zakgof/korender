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
import kotlin.math.sqrt

@Composable
fun LTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val lTreeDef = LTreeDef {
        sqrt(it.x * it.x + it.z * it.z) - sqrt(it.y * 0.1f) * (1 - it.y * 0.1f) * 10f
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
                scale(0.08f, (branch.tail - branch.head).length() * 1.05f, 0.1f)
                    .rotate(Quaternion.shortestArc(1.y, (branch.tail - branch.head).normalize()))
                    .translate(branch.head)
                    .rotate(1.y, frameInfo.time * 0.1f)
            }.forEach { Instance(it) }
        }
    )

    Renderable(
        base(color = ColorRGBA.Green),
        mesh = sphere(0.1f),
        instancing = instancing("attr", lTree.attractors.size, dynamic = false) {
            lTree.attractors.forEach { Instance(translate(it)) }
        }
    )
}