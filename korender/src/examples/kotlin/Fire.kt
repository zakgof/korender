

import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, -1f, 15f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.billboard().build(gpu)
    val shader = Shaders.create(gpu,"billboard.vert", "fire.frag")
    val material = Materials.create(shader, StockUniforms(gpu).apply {
        xscale = 2.0f
        yscale = 10.0f
        static("strength", 4.0f)
    })

    add(SimpleRenderable(mesh, material))

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        println("FPS=~${frameInfo.avgFps}")
    }
}