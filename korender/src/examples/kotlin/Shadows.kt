
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.*
import com.zakgof.korender.projection.FrustumProjection
import kotlin.math.sin

fun main(): Unit = korender(LwjglPlatform()) {

    light = Vec3(1f,-1f,1f).normalize()
    val flyCamera = FlyCamera(platform, Vec3(-2.0f, 3f, 20f));
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val plate = Meshes.cube(1f) {
        transformPos(Transform().scale(8f, 1f, 5f).translate(-1.6f.y)) // TODO: vertex transformation
    }.build(gpu)
    val material = Materials.standard(gpu, "SHADOW_RECEIVER", "PCSS") {
        colorFile = "/sand.jpg"
    }

    val rcube = SimpleRenderable(Meshes.cube(1.5f).build(gpu), material)
    val rsphere = SimpleRenderable(Meshes.sphere(1.5f).build(gpu), material)
    val rplate = SimpleRenderable(plate, material)

    shadower.add(rcube)
    shadower.add(rsphere)
    add(rcube)
    add(rsphere)
    add(rplate)

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        rcube.transform = Transform().rotate(1.x, -FloatMath.PIdiv2).rotate(1.y, frameInfo.nanoTime * 1e-10f)
        rsphere.transform = Transform().translate(Vec3(-4.0f, 2.0f + sin(frameInfo.nanoTime * 1e-9f), 0.0f));
        println("FPS=~${frameInfo.avgFps}")
    }

}

