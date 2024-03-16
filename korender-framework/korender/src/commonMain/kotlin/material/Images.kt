package com.zakgof.korender.material

import com.zakgof.korender.math.Color
import java.awt.image.BufferedImage
import kotlin.math.min

object Images {

    fun create(
        resX: Int,
        resY: Int,
        block: (pixX: Int, pixY: Int) -> Color
    ): BufferedImage {
        val image = BufferedImage(resX, resY, BufferedImage.TYPE_3BYTE_BGR)
        val raster = image.raster
        val color = FloatArray(3)
        for (pixX in 0 until resX) {
            for (pixY in 0 until resY) {
                val rgb = block(pixX, pixY)
                color[0] = min(rgb.b, 1f) * 255f
                color[1] = min(rgb.g, 1f) * 255f
                color[2] = min(rgb.r, 1f) * 255f
                raster.setPixel(pixX, resY - 1 - pixY, color)
            }
        }
        return image
    }
}
