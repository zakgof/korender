
import com.zakgof.korender.Bucket
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 0f, 15f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    var startTime = 0f;
    val mesh = Meshes.billboard().build(gpu)
    val shader = Shaders.create(gpu, "billboard.vert", "smoke.frag")
    val material = Materials.create(shader, StockUniforms(gpu).apply {
        xscale = 12.0f
        yscale = 12.0f
        dynamic("startTime") { startTime }
    })
    add(SimpleRenderable(mesh, material), Bucket.TRANSPARENT)

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        startTime = frameInfo.nanoTime * 1e-9f - 1.5f;
        println("FPS=~${frameInfo.avgFps}")
    }
}