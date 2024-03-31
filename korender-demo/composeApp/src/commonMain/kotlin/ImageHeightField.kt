
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.math.Vec3

class ImageHeightField(private val image: Image, private val cell: Float, private val elevationRatio: Float) {

    fun elevation(x: Float, y: Float): Float {
        val xfr = (x / cell + image.width * 0.5f)
        val yfr = (y / cell + image.height * 0.5f)

        val xx = xfr.toInt()
        val yy = yfr.toInt()
        val h0 = image.pixel(xx, yy).r
        val hx = image.pixel(xx + 1, yy).r
        val hy = image.pixel(xx, yy + 1).r

        val h = h0 + (hx - h0) * (xfr - xx) + (hy - h0) * (yfr - yy)
        return h * elevationRatio
    }

    fun normal(x: Float, y: Float): Vec3 {
        val delta = cell * 0.5f
        val e0 = elevation(x, y)
        val ex = elevation(x + delta, y)
        val ey = elevation(x, y + delta)
        return Vec3(e0 - ex, delta, e0 - ey).normalize()
    }
}