import com.zakgof.korender.Bucket
import com.zakgof.korender.Renderable
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import kotlin.random.Random

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 0.5f, 15f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

//    add(
//        SimpleRenderable(
//            Meshes.screenQuad().build(gpu),
//            Materials.create(
//                Shaders.create(gpu, "screen.vert", "cloudsky.frag")
//            )
//        ), Bucket.SKY
//    )

    var startTime = 0f;
    val numSmokeParticles = 10
    val particles = Array(numSmokeParticles) { SmokeParticle() }
    val mesh = Meshes.billboard().instancing(numSmokeParticles).build(gpu)
    val shader = Shaders.create(gpu, "billboard.vert", "smoke.frag")
    val material = Materials.create(shader, StockUniforms(gpu).apply {
        xscale = 12.0f
        yscale = 12.0f
        dynamic("startTime") { startTime }
    })
    add(SimpleRenderable(mesh, material), Bucket.TRANSPARENT)

    add(addFire(gpu), Bucket.TRANSPARENT)

    onFrame = { frameInfo ->
        startTime = frameInfo.nanoTime * 1e-9f - 1.0f
        camera = flyCamera.idle(frameInfo.dt)
        println("FPS=~${frameInfo.avgFps}")

        particles.forEach {
            it.update(frameInfo.dt * 1e-9f)
        }
        particles.sortBy { (camera.mat4() * it.pos).z }
        for (i in particles.indices) {
            val particle = particles[i]
            for (v in 0 until 4) {
                mesh.updateVertex(i * 4 + v) {
                    it.pos = particle.pos
                    it.scale = Vec2(particle.scale, particle.scale)
                }
            }
        }
        mesh.updateGpu()
    }
}

fun addFire(gpu: Gpu): Renderable {
    val mesh = Meshes.billboard().build(gpu)
    val shader = Shaders.create(gpu, "billboard.vert", "fire.frag")
    val material = Materials.create(shader, StockUniforms(gpu).apply {
        xscale = 1.0f
        yscale = 5.0f
        static("strength", 4.0f)
    })
    return SimpleRenderable(mesh, material)
}

class SmokeParticle {

    var pos = Vec3(0f, -1f, 0f)
    var scale = 0.3f;
    var ttl = Random.nextFloat() * 5.0f

    fun update(dt: Float) {
        pos += 1.y * dt
        scale += dt * (0.1f + Random.nextFloat() * 0.3f)
        ttl -= dt
        if (ttl < 0) {
            pos = Vec3(0f, -1f, Random.nextDouble(-0.4, 0.4).toFloat())
            scale = 0.3f;
            ttl = 5.0f + Random.nextFloat() * 5.0f;
        }
    }
}