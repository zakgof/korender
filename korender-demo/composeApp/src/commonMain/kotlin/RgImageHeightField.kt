
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.math.Vec3

/*
*   R: high byte
*   G: low byte
*   B: 0
*/
class RgImageHeightField(private val image: Image, private val cell: Float, private val elevationRatio: Float) {

    fun elevation(x: Float, y: Float): Float {
        val xfr = (x / cell + image.width * 0.5f)
        val yfr = (y / cell + image.height * 0.5f)

        val xx = xfr.toInt()
        val yy = yfr.toInt()
        val h0 = pixel(xx, yy)
        val hx = pixel(xx + 1, yy)
        val hy = pixel(xx, yy + 1)

        val h = h0 + (hx - h0) * (xfr - xx) + (hy - h0) * (yfr - yy)
        return h * elevationRatio
    }

    fun pixel(xx: Int, yy: Int): Float {
        val color = image.pixel(xx, yy)
        val r = color.r * 255f
        val g = color.g * 255f
        return (r * 256 + g) / 65535.0f
    }

    fun normal(x: Float, y: Float): Vec3 {
        val delta = cell * 0.5f
        val e0 = elevation(x, y)
        val ex = elevation(x + delta, y)
        val ey = elevation(x, y + delta)
        return Vec3(e0 - ex, delta, e0 - ey).normalize()
    }
}