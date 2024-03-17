package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import kotlin.random.Random

@Composable
fun InstancedBillboardsExample() = Korender {

    onResize = {
        projection =
            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val particleNum = 1000
    val particles = Array(particleNum) { Particle(Random.nextDouble(5.0).toFloat()) }

    Scene {
        InstancedBillboards(
            id = "particles",
            count = particleNum,
            material = {
                colorFile = "/splat.png"
            }
        ) {
            for (particle in particles) {
                val scale = (5.0f - particle.ttl) * 0.3f
                Instance(
                    pos = particle.pos,
                    scale = Vec2(scale, scale)
                )
            }
        }
    }

    onFrame = { frameInfo ->
        particles.forEach {
            it.update(frameInfo.dt)
        }
        println("FPS=~${frameInfo.avgFps}")
    }
}

class Particle(initTtl: Float = 5.0f) {
    var ttl = initTtl
    var pos = Vec3(-2f, -2f, 0f)
    private var v = Vec3.random() + Vec3(4f, 8f, 0f)
    fun update(dt: Float) {
        v += -5.y * dt
        pos += v * dt
        ttl -= dt
        if (ttl < 0) {
            ttl = 5.0f
            pos = Vec3(-2f, -2f, 0f)
            v = Vec3.random() + Vec3(4f, 8f, 0f)
        }
    }


}