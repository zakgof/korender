
import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = 10.z, dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }
    val material = Materials.standard(gpu, "TRIPLANAR") {
        colorFile = "/sand.png"
        triplanarScale = 0.4f
    }

    for (x in -60..60) {
        for (y in -60..60) {
            val mesh = Meshes.sphere(0.5f) {
                transformPos(Transform().translate(Vec3(x.toFloat(), y.toFloat(), 0f)))
            }.build(gpu)
            add(Renderable(mesh, material))
        }
    }

    onFrame = { frameInfo ->
        println("FPS=${frameInfo.avgFps} ~FPS=${1e9 / frameInfo.dt} Renderables ${frameInfo.visibleRenderableCount}/${frameInfo.renderableCount}")
    }
}