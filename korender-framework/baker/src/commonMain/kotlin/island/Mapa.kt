package island

import com.zakgof.korender.math.Vec2
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Mapa(val side: Int) {

    private val points: FloatArray = FloatArray(side * side)

    fun populate(function: (pt: Vec2) -> Float) {
        for (xx in 0 until side) {
            for (zz in 0 until side) {
                points[xx + zz * side] = function(toVec2(xx, zz))
            }
        }
    }

    fun toFl(pixel: Int): Float = (pixel.toFloat() + 0.5f) / side
    fun toVec2(xx:Int, yy: Int): Vec2 = Vec2(toFl(xx), toFl(yy))

    fun toPix(coord: Float): Int = (coord * side - 0.5f).toInt()

    fun save2(path: String) {
        val bi = BufferedImage(side, side, BufferedImage.TYPE_3BYTE_BGR)
        val raster = bi.raster
        val pixel = IntArray(3)
        for (xx in 0 until side) {
            for (zz in 0 until side) {
                val h = (points[xx + zz * side] * 65535).toInt()
                pixel[0] = h and 255
                pixel[1] = h shr 8
                raster.setPixel(xx, zz, pixel)
            }
        }
        val f = File(path)
        ImageIO.write(bi, "png", f)
    }

    fun save(path: String) {
        val bi = BufferedImage(side, side, BufferedImage.TYPE_BYTE_GRAY)
        val raster = bi.raster
        for (xx in 0 until side) {
            for (zz in 0 until side) {
                val h = (points[xx + zz * side] * 255).toInt().coerceIn(0, 255)
                raster.setSample(xx, zz, 0, h)
            }
        }
        val f = File(path)
        ImageIO.write(bi, "png", f)
    }

    operator fun get(pt: Vec2): Float {
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

    fun set(xx: Int, zz: Int, value: Float) {
        points[xx + zz * side] = value.coerceIn(0f, 1f)
    }

    fun get(xx: Int, zz: Int): Float = points[xx + zz * side]

    fun gradient(pt: Vec2): Vec2 {
        val gridX = pt.x * side - 0.5f
        val gridY = pt.y * side - 0.5f

        val i = gridX.toInt().coerceIn(0, side - 1)
        val j = gridY.toInt().coerceIn(0, side - 1)

        val iMinus1 = (i - 1).coerceIn(0, side - 1)
        val iPlus1 = (i + 1).coerceIn(0, side - 1)
        val jMinus1 = (j - 1).coerceIn(0, side - 1)
        val jPlus1 = (j + 1).coerceIn(0, side - 1)

        val hLeft = get(iMinus1, j)
        val hRight = get(iPlus1, j)
        val dhdx = (hRight - hLeft) / (2.0f / side)

        val hBottom = get(i, jMinus1)
        val hTop = get(i, jPlus1)
        val dhdy = (hTop - hBottom) / (2.0f / side)

        return Vec2(dhdx, dhdy)
    }

}