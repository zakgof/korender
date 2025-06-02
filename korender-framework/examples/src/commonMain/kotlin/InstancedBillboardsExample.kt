package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.random.Random

@Composable
fun InstancedBillboardsExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val particleNum = 1000
    val particles = Array(particleNum) { Particle(Random.nextFloat() * 5f) }

    Frame {
        AmbientLight(White)
        Billboard(
            base(
                color = ColorRGBA.Red,
                colorTexture = texture("texture/splat.png")
            ),
            transparent = true,
            instancing = billboardInstancing(
                id = "particles",
                count = particleNum,
                dynamic = true
            ) {
                for (particle in particles) {
                    particle.update(frameInfo.dt)
                    val scale = (5.0f - particle.ttl) * 0.3f
                    Instance(
                        pos = particle.pos,
                        scale = Vec2(scale, scale)
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

class Particle(initTtl: Float = 5.0f) {
    var ttl = initTtl
    var pos = Vec3(-2f, -2f, 0f)
    private var v = Vec3.random() + Vec3(2f, 7f, 0f)
    fun update(dt: Float) {
        v += -5.y * dt
        pos += v * dt
        ttl -= dt
        if (ttl < 0) {
            ttl = 5f
            pos = Vec3(-2f, -2f, 0f)
            v = Vec3.random() + Vec3(2f, 7f, 0f)
        }
    }
}