
import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Attributes.NORMAL
import com.zakgof.korender.geometry.Attributes.POS
import com.zakgof.korender.geometry.Attributes.TEX
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = Vec3(0f, 3f, 20f), dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val terrain = Renderable(
        mesh = Meshes.create(4, 6, POS, NORMAL, TEX) {
            vertices(-100f, 0f, -100f, 0f, 1f, 0f, 0f, 0f)
            vertices(-100f, 0f,  100f, 0f, 1f, 0f, 0f, 50f)
            vertices( 100f, 0f,  100f, 0f, 1f, 0f, 50f, 50f)
            vertices( 100f, 0f, -100f, 0f, 1f, 0f, 50f, 0f)
            indices(0, 1, 2, 0, 2, 3)
        }.build(gpu),
        material = Materials.standard(gpu, "APERIODIC") {
            colorFile = "/grass-aperiodic.png"
            aperiodicFile = "/aperiodic.png"
        })
    add(terrain)
}