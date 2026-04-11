package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun InstancedBillboardsExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {

    val particleNum = 2000
    val particles = Array(particleNum) { Particle(Random.nextFloat()) }

    Frame {

        TestExchange.report(frameInfo)
        AmbientLight(White)
        camera = camera(11.z, -1.z, 1.y)
        projection = projection(2f * width / height, 2f, 1f, 200f, frustum())
        Billboard(
            billboard {
                color = ColorRGBA(1f, 1f, 1f, 0.3f)
                colorTexture = texture("texture/splat.png")
            },
            transparent = true,
            instancing = billboardInstancing(
                id = "particles",
                count = particleNum,
                dynamic = true
            ) {
                repeat (particleNum) { i ->
                    val particle = particles[i]
                    if (!particle.update(frameInfo.dt)) {
                        particles[i] = Particle()
                    }
                    Instance(
                        pos = particle.pos,
                        scale = Vec2(particle.scale, particle.scale)
                    )
                }
            }
        )
        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

class Particle(startTtl: Float = 1.0f) {
    var ttl = startTtl
    val startPos = Vec3(-10f + 20f * Random.nextFloat(), 10f,  10f * Random.nextFloat())
    val scale = 0.1f + 0.1f * Random.nextFloat()
    val pos
        get() = startPos - (1f - ttl).y * 20f + (1f - ttl).z * 10f + (0.5f * sin(ttl * 8f + startPos.z * 8f)).x
    fun update(dt: Float): Boolean {
        ttl -= dt * 0.2f
        return ttl > 0
    }
}

