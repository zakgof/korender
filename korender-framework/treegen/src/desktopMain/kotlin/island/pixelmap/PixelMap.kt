package island.pixelmap

import com.zakgof.korender.math.Vec2
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

abstract class PixelMap<P>(val side: Int) {

    @Suppress("UNCHECKED_CAST")
    private val points: Array<P> = arrayOfNulls<Any?>(side * side) as Array<P>

    fun populate(function: (pt: Vec2) -> P) {
        for (xx in 0 until side) {
            for (zz in 0 until side) {
                points[xx + zz * side] = function(toVec2(xx, zz))
            }
        }
    }

    fun toFl(pixel: Int): Float = (pixel + 0.5f) / side
    fun toPix(coord: Float): Int = (coord * side - 0.5f).toInt()
    fun toVec2(xx: Int, yy: Int): Vec2 = Vec2(toFl(xx), toFl(yy))


    fun save(file: File) {
        val bi = BufferedImage(side, side, imageType)
        val raster = bi.raster
        val pixel = IntArray(pixelChannels)
        for (xx in 0 until side) {
            for (zz in 0 until side) {
                fillPixel(get(xx, zz), pixel)
                raster.setPixel(xx, zz, pixel)
            }
        }
        ImageIO.write(bi, "png", file)
    }

    fun set(xx: Int, zz: Int, value: P) {
        points[xx + zz * side] = value
    }

    fun get(xx: Int, zz: Int): P = points[xx + zz * side]

    abstract val imageType: Int

    abstract val pixelChannels: Int

    abstract fun fillPixel(value: P, pixel: IntArray)
}

