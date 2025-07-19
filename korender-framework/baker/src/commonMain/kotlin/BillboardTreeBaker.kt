package com.zakgof.korender.baker

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.pow

@Composable
fun BillboardTreeBaker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val basePath = "D:\\p\\dev\\assets\\" // TODO

    val metaball = Metaball(20f, 3.0f, 8000, 48) { (it * 0.05f).pow(0.1f) * (1f - it * 0.05f) * 10f }
    // val metaball = Metaball(20f, 1.0f) { sqrt(it * 0.05f) * (1f - it * 0.05f) * 10f }
    // val metaball = Metaball(20f, 4.0f, 4096, 32) { (it * 0.05f) * (1f - it * 0.05f) * 20f }

    val metaballTree = MetaballTree(this, metaball, "cone-tree")

    val treeImage = captureFrame(512, 512, camera(60.z, -1.z, 1.y), ortho(50f, 50f, 1f, 100f)) {
        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2.0f, 0.0f, -2.0f), white(3f))
        tree(metaballTree)
    }
    saveImage(treeImage, "png", basePath + "tree.png")

    Frame {
        projection = projection(3f * width / height, 3f, 3f, 100f)
        camera = camera(60.z, -1.z, 1.y)
        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2.0f, 0.0f, -2.0f), white(3f))

        tree(metaballTree)
    }
}

private fun FrameContext.tree(metaballTree: MetaballTree) {
    metaballTree.render(this)
    Renderable(
        base(
            color = ColorRGBA(0x302010FF)
        ),
        mesh = cylinderSide(30f, 1.0f),
        transform = translate(-10.y)
    )
}
