package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun InstancedBillboardsExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val particleNum = 1000
    val particles = Array(particleNum) { Particle(Random.nextDouble(5.0).toFloat()) }

    Frame {
        AmbientLight(White)
        InstancedBillboards(
            base(
                color = ColorRGBA.Red,
                colorTexture = texture("texture/splat.png")
            ),
            id = "particles",
            count = particleNum,
            transparent = true
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