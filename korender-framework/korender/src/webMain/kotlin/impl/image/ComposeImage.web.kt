package com.zakgof.korender.impl.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlinx.browser.document
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.set
import org.khronos.webgl.toUint8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.ExperimentalUnsignedTypes
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class)
internal actual fun composeImageFromArgbPixels(width: Int, height: Int, argbPixels: IntArray): ImageBitmap {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height

    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    val imageData = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble())
    val rgbaBytes = argbPixels.toRgbaBytes().asUByteArray().toUint8Array()
    (imageData.data as Uint8ClampedArray).set(rgbaBytes)
    ctx.putImageData(imageData, 0.0, 0.0)

    // Compose Web does not expose a public raw-pixel writer, so we round-trip through PNG.
    val pngBytes = Base64.decode(canvas.toDataURL("image/png").substringAfter(","))
    return decodeToImageBitmap(pngBytes)
}

private fun IntArray.toRgbaBytes(): ByteArray {
    val bytes = ByteArray(size * 4)
    var src = 0
    var dst = 0
    while (src < size) {
        val argb = this[src++]
        bytes[dst++] = ((argb shr 16) and 0xFF).toByte()
        bytes[dst++] = ((argb shr 8) and 0xFF).toByte()
        bytes[dst++] = (argb and 0xFF).toByte()
        bytes[dst++] = ((argb ushr 24) and 0xFF).toByte()
    }
    return bytes
}
