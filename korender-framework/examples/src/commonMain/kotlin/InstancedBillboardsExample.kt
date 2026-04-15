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

    val particleNum = 4000
    val particles = Array(particleNum) { Particle(Random.nextFloat(), width.toFloat() / height) }

    Frame {
        val aspect = width.toFloat() / height
        TestExchange.report(frameInfo)
        AmbientLight(White)
        camera = camera(-1.z, 1.z, 1.y)
        projection = projection(2f * aspect, 2f, 1f, 31f, frustum())
        Billboard(
            billboard {
                colorTexture = texture("texture/splat.png")
            },
            transparent = true,
            instancing = billboardInstancing(
                id = "particles",
                count = particleNum,
                dynamic = true,
                POSITION_BILLBOARD_INSTANCING, SCALE_BILLBOARD_INSTANCING, COLOR_BILLBOARD_INSTANCING
            ) {
                repeat(particleNum) { i ->
                    val particle = particles[i]
                    if (!particle.update(frameInfo.dt)) {
                        particles[i] = Particle(1f, aspect)
                    }
                    Instance(
                        pos = particle.pos,
                        scale = Vec2(particle.scale, particle.scale),
                        color = particle.color
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

class Particle(startTtl: Float = 1.0f, aspect: Float) {
    var ttl = startTtl
    val startPos = Vec3((-1f + 2f * Random.nextFloat()) * 30f * aspect, 30f, 30f * Random.nextFloat())
    val scale = 0.4f + 0.3f * Random.nextFloat()
    val color = ColorRGBA(0.6f + 0.4f * Random.nextFloat(), 0.7f + 0.3f * Random.nextFloat(), 1f, 0.25f)
    val pos
        get() = startPos - (1f - ttl).y * 60f + (2f * sin(ttl * 8f + startPos.z * 8f)).x

    fun update(dt: Float): Boolean {
        ttl -= dt * 0.1f
        return ttl > 0
    }
}

