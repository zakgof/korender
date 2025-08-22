package com.zakgof.korender.examples.island

import com.zakgof.korender.Image
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.floor

@OptIn(ExperimentalCoroutinesApi::class)
class Height(private val deferredMap: Deferred<Image>) {

    private fun pixel(image: Image, x: Int, y: Int): Float {
        val color = image.pixel(x, y)
        return (color.g * 255.0f + color.r) * 16.0f - 256.0f * 16.0f * 0.1f;
    }

    fun texToWorld(n: Vec3) = Vec3(
        (-0.5f + n.x) * 32f * 512f,
        n.y * 256f * 16f - 256f * 16f * 0.1f + 64f,
        (-0.5f + n.z) * 32f * 512f
    )

    fun texToWorld(n: Vec2) = Vec2(
        (-0.5f + n.x) * 32f * 512f,
        (-0.5f + n.y) * 32f * 512f
    )

    fun worldToTex(wx: Float, wz: Float) = Vec2(
        wx / (32f * 512f) + 0.5f,
        wz / (32f * 512f) + 0.5f
    )

    fun texSample(tex: Vec2): Float {

        if (!deferredMap.isCompleted)
            return 0f

        val image = deferredMap.getCompleted()


        val fx = tex.x.coerceIn(0f, 1f) * (image.width  - 1).toFloat()
        val fy = tex.y.coerceIn(0f, 1f) * (image.height - 1).toFloat()

        val x0 = floor(fx).toInt()
        val y0 = floor(fy).toInt()
        val x1 = (x0 + 1).coerceIn(0, image.width  - 1)
        val y1 = (y0 + 1).coerceIn(0, image.height - 1)

        val tx = fx - x0
        val ty = fy - y0

        val h00 = pixel(image, x0, y0)
        val h10 = pixel(image,x1, y0)
        val h01 = pixel(image,x0, y1)
        val h11 = pixel(image,x1, y1)

        val hx0 = h00 + (h10 - h00) * tx
        val hx1 = h01 + (h11 - h01) * tx
        return hx0 + (hx1 - hx0) * ty
    }


    fun texToWorld(tex: Vec2, elevation: Float = 0f): Vec3 {
        val ts = texSample(tex)
        val w = texToWorld(tex)
        return Vec3(w.x, ts + elevation, w.y)
    }

    fun world(world: Vec3, elevation: Float = 0f): Vec3 {
        val tex = worldToTex(world.x, world.z)
        val ts = texSample(tex)
        return Vec3(world.x, ts + elevation, world.z)
    }

}