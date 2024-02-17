
import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.*
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    light = Vec3(1f,-1f,0f).normalize()
    camera = DefaultCamera(pos = Vec3(0f, 1.5f, 20f), dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val cube = Meshes.cube(1.5f).build(gpu)
    val plate = Meshes.cube(1f) {
        transformPos(Transform().scale(5f, 1f, 5f).translate(-1.6f.y))
    }.build(gpu)
    val material = Materials.standard(gpu, "SHADOW_RECEIVER") {
        colorFile = "/sand.png"
    }

    val rcube = Renderable(cube, material)
    val rplate = Renderable(plate, material)

    shadower!!.add(rcube)
    add(rcube)
    add(rplate)

    onFrame = { frameInfo ->
        rcube.transform = Transform().rotate(1.x, -FloatMath.PI * 0.5f).rotate(1.y, frameInfo.nanoTime * 1e-10f)
        println("FPS=~${1e9 / frameInfo.dt} Renderables ${frameInfo.visibleRenderableCount}/${frameInfo.renderableCount}")
    }

}

