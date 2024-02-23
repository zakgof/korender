
import com.zakgof.korender.Bucket
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
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
    val mesh = Meshes.sphere(0.5f)
        .instancing(100) { i, v ->
            v.pos = v.pos!! + Vec3(i / 10 - 5f, i % 10 - 5f, 0f)
        }
        .build(gpu)

    add(SimpleRenderable(mesh, material), Bucket.TRANSPARENT)

    onFrame = { frameInfo ->
        println("FPS=~${frameInfo.avgFps}")
    }
}