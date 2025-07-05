package com.zakgof.korender.baker

import com.zakgof.korender.Image
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import javax.imageio.ImageIO

fun saveImage(img: Image, format: String, path: String) {
    val bytes = img.toRaw()
    val bi = BufferedImage(img.width, img.height, TYPE_INT_ARGB)
    val raster = bi.raster
    val pixel = IntArray(4)
    for (x in 0 until img.width) {
        for (y in 0 until img.height) {
            pixel[0] = bytes[(x + y * img.width) * 4 + 0].toUByte().toInt()
            pixel[1] = bytes[(x + y * img.width) * 4 + 1].toUByte().toInt()
            pixel[2] = bytes[(x + y * img.width) * 4 + 2].toUByte().toInt()
            pixel[3] = bytes[(x + y * img.width) * 4 + 3].toUByte().toInt()
            raster.setPixel(x, img.height - 1 - y, pixel)
        }
    }
    ImageIO.write(bi, format, File(path))
}