package island.pixelmap

import java.awt.image.BufferedImage

class ChannelMap(side: Int) : PixelMap<FloatArray>(side) {
    override val imageType = BufferedImage.TYPE_4BYTE_ABGR
    override val pixelChannels = 4

    override fun fillPixel(value: FloatArray, pixel: IntArray) {
        pixel[0] = (value[0] * 255).toInt().coerceIn(0, 255)
        pixel[1] = (value[1] * 255).toInt().coerceIn(0, 255)
        pixel[2] = (value[2] * 255).toInt().coerceIn(0, 255)
        pixel[3] = (value[3] * 255).toInt().coerceIn(0, 255)
    }
}

fun channel(channel: Int, weight: Float) =
    FloatArray(8).apply { this[channel] = weight }

fun channels(ch1: Int, ch2: Int, weight: Float) =
    FloatArray(8).apply {
        this[ch1] = 1f - weight
        this[ch2] = weight
    }


