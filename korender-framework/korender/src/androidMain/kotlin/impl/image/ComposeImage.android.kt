package com.zakgof.korender.impl.image

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun composeImageFromArgbPixels(width: Int, height: Int, argbPixels: IntArray): ImageBitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(argbPixels, 0, width, 0, 0, width, height)
    return bitmap.asImageBitmap()
}
