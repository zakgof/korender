
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Images
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.*
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.projection.FrustumProjection
import de.javagl.obj.ObjReader

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = Vec3(0f, 0f, 20f), dir = -1.z, up = 1.y)
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }


    val obj = ObjReader.read(Images.javaClass.getResourceAsStream("/cat-red.obj"))
    val mb = Meshes.create(obj)
    val material = Materials.standard(gpu) {
        textureFile = "/cat-red.jpg"
        ambient = 1.0f
        diffuse = 1.0f
        specular = 0.0f
    }
    val renderable = renderable(mb, material)
    add(renderable)

    onFrame = { kc ->
        renderable.transform = Transform().scale(0.1f).rotate(1.x, -PI * 0.5f).rotate(1.y, kc.nanoTime * 1e-10f)
        println("FPS=${kc.avgFps} ~FPS=${1e9 / kc.dt} Renderables ${kc.visibleRenderableCount}/${kc.renderableCount}")
    }
}
