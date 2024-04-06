package com.zakgof

import java.awt.image.BufferedImage

object Images {

    fun create(
        resX: Int,
        resY: Int,
        block: (pixX: Int, pixY: Int) -> FloatArray
    ): BufferedImage {
        val image = BufferedImage(resX, resY, BufferedImage.TYPE_3BYTE_BGR)
        val raster = image.raster
        for (pixX in 0 until resX) {
            for (pixY in 0 until resY) {
                val rgb = block(pixX, pixY)
                raster.setPixel(pixX, pixY, rgb)
            }
        }
        return image
    }

    fun createi(
        resX: Int,
        resY: Int,
        block: (pixX: Int, pixY: Int) -> IntArray
    ): BufferedImage {
        val image = BufferedImage(resX, resY, BufferedImage.TYPE_3BYTE_BGR)
        val raster = image.raster
        for (pixX in 0 until resX) {
            for (pixY in 0 until resY) {
                val rgb = block(pixX, pixY)
                raster.setPixel(pixX, pixY, rgb)
            }
        }
        return image
    }
}