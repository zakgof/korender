
import com.zakgof.korender.Renderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 2f, 20f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.heightMap(256, 256, 0.1f) { x, y ->
        0.4f * sin(0.3f * x.toFloat()) * sin(0.3f * y.toFloat())
    }.build(gpu)
    val material = Materials.standard(gpu) {
        colorFile = "/sand.png"
        diffuse = 0.8f
        ambient = 0.2f
        specular = 2.0f
    }
    val renderable = Renderable(mesh, material)
    add(renderable)

    onFrame = { frameInfo ->
        flyCamera.idle(frameInfo.dt)
        camera = flyCamera.camera
        println("FPS=~${frameInfo.avgFps}")
    }
}
