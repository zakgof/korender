import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.ShaderBuilder
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
        Renderable(
            Meshes.screenQuad().build(gpu),
            Materials.create(
                ShaderBuilder("screen.vert", "starsky.frag").build(gpu)
            )
        )
    )

    onFrame = { frameInfo ->
        println("FPS=~${1e9 / frameInfo.dt} Renderables ${frameInfo.visibleRenderableCount}/${frameInfo.renderableCount}")
    }
}