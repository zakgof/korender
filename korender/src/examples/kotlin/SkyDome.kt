
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 0f, 20f));
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    add(
        SimpleRenderable(
            Meshes.screenQuad().build(gpu),
            Materials.create(
                Shaders.create(gpu, "screen.vert", "starsky.frag")
            )
        ),
    )

    onFrame = { frameInfo ->
        flyCamera.idle(frameInfo.dt)
        camera = flyCamera.camera
        println("FPS=~${frameInfo.avgFps}")
    }
}