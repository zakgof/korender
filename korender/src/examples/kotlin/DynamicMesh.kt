
import com.zakgof.korender.Bucket
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.FloatMath.sin
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = Vec3(0f, 0f, 20f), dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.sphere(15f).build(gpu, true)
    val originalPointPositions = mesh.positions();

    val material = Materials.standard(gpu, "TRIPLANAR") {
        colorFile = "/sand.jpg"
        triplanarScale = 0.01f
    }
    val renderable = SimpleRenderable(mesh, material)
    add(renderable, Bucket.TRANSPARENT)

    onFrame = { frameInfo ->
        renderable.transform = Transform().scale(0.1f).rotate(1.y, frameInfo.time * 0.1f)
        for (i in 0..<mesh.vertices) {
            mesh.updateVertex(i) { it.pos = perturb(originalPointPositions[i], frameInfo.time) }
        }
        mesh.updateGpu()
        println("FPS=~${frameInfo.avgFps}")
    }
}

fun perturb(orig: Vec3, time: Float) = Vec3(orig.x * (1.0f + 0.6f * sin(time)), orig.y, orig.z)

