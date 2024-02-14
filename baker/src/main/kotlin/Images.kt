package com.zakgof

import java.awt.image.BufferedImage
import kotlin.math.min

object Images {

    fun create(
        resX: Int,
        resY: Int,
        block: (pixX: Int, pixY: Int) -> FloatArray
    ): BufferedImage {
        val image = BufferedImage(resX, resY, BufferedImage.TYPE_3BYTE_BGR)
        val raster = image.raster
        val color = FloatArray(3)
        for (pixX in 0 until resX) {
            for (pixY in 0 until resY) {
                val rgb = block(pixX, pixY)
                raster.setPixel(pixX, pixY, rgb)
            }
        }
        return image
    }
}