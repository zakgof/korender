package insecto

import com.zakgof.korender.Image
import com.zakgof.korender.math.Vec3

/*
*   R: high byte
*   G: low byte
*   B: 0
*/
class RgImageHeightField(val image: Image, private val cell: Float, private val elevationRatio: Float) :
    HeightField {

    override fun elevation(x: Float, z: Float): Float {
        val xfr = (x / cell + (image.width - 1) * 0.5f)
        val zfr = (z / cell + (image.height - 1) * 0.5f)

        val xx = xfr.toInt()
        val zz = zfr.toInt()

        val hx = pixel(xx + 1, zz)
        val hz = pixel(xx, zz + 1)

        return if ((xfr - xx) + (zfr - zz) < 1.0f) {
            val h0 = pixel(xx, zz)
            val h = h0 + (hx - h0) * (xfr - xx) + (hz - h0) * (zfr - zz)
            h * elevationRatio
        } else {
            val h1 = pixel(xx + 1, zz + 1)
            val h = h1 + (hx - h1) * (zz + 1 - zfr) + (hz - h1) * (xx + 1 - xfr)
            h * elevationRatio
        } - 3.0f
    }

    fun pixel(xx: Int, zz: Int): Float {
        if (xx < 0 || zz < 0 || xx >= image.width || zz >= image.height) {
            return 0f
        }
        val color = image.pixel(xx, zz)
        val r = color.r * 255f
        val g = color.g * 255f
        return (r * 256 + g) / 65535.0f
    }

    override fun normal(x: Float, y: Float): Vec3 {
        val delta = cell * 0.1f
        val e0 = elevation(x, y)
        val ex = elevation(x + delta, y)
        val ey = elevation(x, y + delta)
        return Vec3(e0 - ex, delta, e0 - ey).normalize()
    }
}