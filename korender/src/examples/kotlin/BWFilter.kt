
import com.zakgof.korender.Bucket
import com.zakgof.korender.Filter
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

    camera = DefaultCamera(pos = Vec3(0f, 0f, 20f), dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    add(
        SimpleRenderable(
            Meshes.sphere(1.5f).build(gpu),
            Materials.standard(gpu) {
                colorFile = "/sand.jpg"
            }),
        Bucket.TRANSPARENT
    )

    addFilter(Filter(gpu, "bw.frag") { null })

    onFrame = { frameInfo ->
        println("FPS=~${frameInfo.avgFps}")
    }
}