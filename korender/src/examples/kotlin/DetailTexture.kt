
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, 20.z)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.quad(5.0f).build(gpu)
    val material = Materials.standard(gpu, "DETAIL", "NO_LIGHT") {
        colorFile = "/lowresmap.jpg"
        detailFile = "/detail.jpg"
        detailScale = 8.0f
        detailRatio = 0.4f
    }
    add(SimpleRenderable(mesh, material))

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        println("FPS=~${frameInfo.avgFps}")
    }
}
