package island.pixelmap

import java.awt.image.BufferedImage

class Float2PixelMap(side: Int) : FloatPixelMap(side) {

    override val imageType = BufferedImage.TYPE_3BYTE_BGR

    override val pixelChannels = 3

    override fun fillPixel(value: Float, pixel: IntArray) {
        val h = (value * 65535).toInt()
        pixel[0] = h and 255
        pixel[1] = h shr 8
    }
}