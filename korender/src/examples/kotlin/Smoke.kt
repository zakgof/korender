
import com.zakgof.korender.Bucket
import com.zakgof.korender.Renderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.DynamicUniformSupplier
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.ShaderBuilder
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection
import kotlin.random.Random

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(0f, 0f, 15f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val mesh = Meshes.billboard().build(gpu)
    val shader = ShaderBuilder("billboard.vert", "smoke.frag").build(gpu)

    val particles = List(50) { SmokeParticle(Random.nextFloat() * 3.0f) }
    particles.map { particle ->
        Materials.create(shader, StockUniforms(gpu).apply {
            xscale = 5.0f
            yscale = 5.0f
        }.uniforms + DynamicUniformSupplier("startTime") { particle.startTime })
    }.map { Renderable(mesh, it) }
        .forEach {
            it.transform = Transform().translate(Vec3.random())
            add(it, Bucket.TRANSPARENT)
        }

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        particles.forEach { it.update(frameInfo.nanoTime * 1e-9f, frameInfo.dt * 1e-9f) }
        println("FPS=~${frameInfo.avgFps}")
    }
}

private class SmokeParticle(initialTtl: Float) {
    var ttl = initialTtl
    var startTime = 0f
    fun update(time: Float, dt: Float) {
        ttl -= dt
        if (ttl < 0) {
            startTime = time
            ttl = Random.nextFloat() * 5.0f
        }
    }
}
