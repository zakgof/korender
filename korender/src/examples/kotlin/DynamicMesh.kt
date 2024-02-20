import com.zakgof.korender.Bucket
import com.zakgof.korender.Renderable
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
        colorFile = "/sand.png"
        triplanarScale = 0.01f
    }
    val renderable = Renderable(mesh, material)
    add(renderable, Bucket.TRANSPARENT)

    onFrame = { frameInfo ->
        renderable.transform = Transform().scale(0.1f).rotate(1.y, frameInfo.nanoTime * 1e-10f)
        for (i in 0..<mesh.vertices) {
            mesh.setPosition(i, perturb(originalPointPositions[i], frameInfo.nanoTime))
        }
        mesh.update()
        println("FPS=~${frameInfo.avgFps}")
    }
}

fun perturb(orig: Vec3, time: Long) = Vec3(orig.x * (1.0f + 0.6f * sin(time * 1e-9f)), orig.y, orig.z)

