
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = 20.z, dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val sphere = renderable(
        gpuMesh = Meshes.sphere(2f).build(gpu),
        gpuMaterial = Materials.standard(gpu, "TRIPLANAR") {
            textureFile = "/sand.png"
            triplanarScale = 0.4f
        })
    add(sphere)

    onFrame = {
        sphere.transform = Transform().rotate(1.y, it.nanoTime * 1e-9f)
    }
}