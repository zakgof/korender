package com.zakgof.korender.impl.image

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun composeImageFromArgbPixels(width: Int, height: Int, argbPixels: IntArray): ImageBitmap
