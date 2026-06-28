package com.zakgof.korender.impl.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage

internal actual fun composeImageFromArgbPixels(width: Int, height: Int, argbPixels: IntArray): ImageBitmap {
    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    bufferedImage.setRGB(0, 0, width, height, argbPixels, 0, width)
    return bufferedImage.toComposeImageBitmap()
}
