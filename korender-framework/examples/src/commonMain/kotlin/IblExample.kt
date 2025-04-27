package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Korender
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.examples.qhull.QuickHull
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, Vec3.ZERO, -1.z)
    OnTouch { freeCamera.touch(it) }
    projection = frustum(3f * width / height, 3f, 3f, 100f)

    val rnd = Random.Default
    val points = (0 until 100).map { Vec3(rnd.nextFloat() - 0.5f, rnd.nextFloat() - 0.5f, rnd.nextFloat() - 0.5f).normalize() * 10.0f}
    val qhMesh = QuickHull(points).run()

    val hullMesh = customMesh("hull", qhMesh.points.size, qhMesh.indexes.size, POS, NORMAL) {
        qhMesh.points.forEach {
            pos(it.pos)
            normal(it.normal)
        }
        qhMesh.indexes.forEach {
            index(it)
        }
    }


    Frame {

        camera = camera(80.z, -1.z, 1.y)
        scene(hullMesh)
        return@Frame

        CaptureEnv(0, 512, Vec3.ZERO, 3f, 100f, insideOut = true) {
            scene(hullMesh)
        }
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)
        Billboard(
            standart {
                baseColorTexture = texture("texture/grass.jpg")
                xscale = 3.0f
                yscale = 3.0f
            },
            fragment("mpr/mpr.frag"),
            position = -4f.z)

        // Sky(cubeSky(0))

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun FrameContext.scene(hullMesh: MeshDeclaration) {
    AmbientLight(white(0.3f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(2f))
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
        },
        mesh = hullMesh
    )

}