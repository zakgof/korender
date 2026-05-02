package island.pixelmap

import com.zakgof.korender.math.Vec2
import java.awt.image.BufferedImage

open class FloatPixelMap(side: Int) : PixelMap<Float>(side) {

    operator fun get(pt: Vec2): Float {
        if (pt.x < 0f || pt.x > 1f || pt.y < 0f || pt.y > 1f)
            return 0f
        val gridX = pt.x * side - 0.5f
        val gridY = pt.y * side - 0.5f
        val i = gridX.toInt().coerceIn(0, side - 1)
        val j = gridY.toInt().coerceIn(0, side - 1)
        val i1 = (i + 1).coerceIn(0, side - 1)
        val j1 = (j + 1).coerceIn(0, side - 1)
        val dx = gridX - i
        val dy = gridY - j
        val height00 = get(i, j)
        val height10 = get(i1, j)
        val height01 = get(i, j1)
        val height11 = get(i1, j1)
        val bottomInterpolation = height00 * (1f - dx) + height10 * dx
        val topInterpolation = height01 * (1f - dx) + height11 * dx
        return bottomInterpolation * (1f - dy) + topInterpolation * dy
    }

    override val imageType = BufferedImage.TYPE_BYTE_GRAY

    override val pixelChannels = 1

    override fun fillPixel(value: Float, pixel: IntArray) {
        pixel[0] = (value * 255f).toInt().coerceIn(0, 255)
    }
}