
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = 20.z, dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.cube( 15f).build(gpu)
    val material = Materials.standard(gpu, "NORMAL_MAP") {
        normalFile = "/normal.png"
        colorFile = "/sand.png"
    }
    val renderable = SimpleRenderable(mesh, material)
    add(renderable)

    onFrame = { frameInfo ->
        renderable.transform = Transform().scale(0.1f).rotate(1.y, frameInfo.nanoTime * 1e-10f)
        println("FPS=~${frameInfo.avgFps}")
    }
}
