
import com.zakgof.korender.Bucket
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 0f, 20f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.billboard().build(gpu)
    val material = Materials.billboard(gpu) {
        colorFile = "/sand.png"
    }

    for (x in -5..5) {
        for (y in -5..5) {
            val renderable = SimpleRenderable(mesh, material)
            renderable.transform = Transform().scale(0.8f).translate(Vec3(x.toFloat(), y.toFloat(), 0f))
            add(renderable, Bucket.TRANSPARENT)
        }
    }

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        println("FPS=~${frameInfo.avgFps} Renderables ${frameInfo.visibleRenderableCount}/${frameInfo.renderableCount}")
    }
}