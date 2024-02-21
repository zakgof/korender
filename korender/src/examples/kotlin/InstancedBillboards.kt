import com.zakgof.korender.Renderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Materials
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import kotlin.random.Random

fun main(): Unit = korender(LwjglPlatform()) {

    val flyCamera = FlyCamera(platform, Vec3(5f, 4f, 20f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val particleNum = 1000
    val particles = Array(particleNum) { Particle(Random.nextDouble(5.0).toFloat()) }
    val mesh = Meshes.billboard().instancing(particleNum).build(gpu)
    val material = Materials.billboard(gpu, "/splat.png")

    add(Renderable(mesh, material))

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        for (i in particles.indices) {
            if (!particles[i].update(frameInfo.dt * 1e-9f))
                particles[i] = Particle()
        }
        particles.sortByDescending { (it.pos - camera.position()).lengthSquared() }
        for (i in particles.indices) {
            val particle = particles[i]
            for (v in 0 until 4) {
                mesh.updateVertex(i * 4 + v) {
                    it.pos = particle.pos
                    val scale = (5.0f - particle.ttl) * 0.3f
                    it.scale = Vec2(scale, scale)
                }
            }
        }
        mesh.updateGpu()
        println("FPS=~${frameInfo.avgFps}")
    }
}

class Particle(initTtl: Float = 5.0f) {
    var ttl = initTtl
    var pos = Vec3.ZERO
    private var v = Vec3.random() + Vec3(4f, 8f, 0f)
    fun update(dt: Float): Boolean {
        v += -5.y * dt
        pos += v * dt
        ttl -= dt
        return ttl > 0
    }

}