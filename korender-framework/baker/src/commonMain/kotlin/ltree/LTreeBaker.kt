package ltree

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun LTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

//    val lTree = LTree(
//        listOf(
//            LTree.Branch(Vec3(0f, 0f, 0f), Vec3(0f, 1f, 0f)),
//            LTree.Branch(Vec3(0f, 1f, 0f), Vec3(0.3f, 3f, 0f)),
//            LTree.Branch(Vec3(0.3f, 3f, 0f), Vec3(0.1f, 4f, 0f))
//        )
//    )

    val lTreeDef = LTreeDef()

    val lTree = generateLTree(lTreeDef)

    Frame {
        AmbientLight(white(0.3f))
        DirectionalLight(Vec3(3f, 0f, 1f), white(1.0f))
        camera = camera(2.y + (-15).z, 1.z, 1.y)
        renderLTree(lTree)
    }
}

fun FrameContext.renderLTree(lTree: LTree) {
    Renderable(
        base(color = ColorRGBA.Blue),
        mesh = cylinderSide(),
        instancing = instancing("trunk", lTree.branches.size, dynamic = false) {
            lTree.branches.map { branch ->
                scale(0.08f, (branch.tail - branch.head).length() * 1.05f, 0.1f)
                    .rotate(Quaternion.shortestArc(1.y, (branch.tail - branch.head).normalize()))
                    .translate(branch.head)
            }.forEach { Instance(it) }
        }
    )
}