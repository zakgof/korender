
import com.zakgof.korender.Skies
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 0f, 20f));
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    // add(Skies.fastClouds(gpu))
    add(Skies.fancyClouds(gpu))
    // add(Skies.stars(gpu))

    onFrame = { frameInfo ->
        flyCamera.idle(frameInfo.dt)
        camera = flyCamera.camera
        println("FPS=~${frameInfo.avgFps}")
    }
}