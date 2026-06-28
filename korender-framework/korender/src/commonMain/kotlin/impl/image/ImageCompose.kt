package com.zakgof.korender.impl.image

import com.zakgof.korender.Image
import com.zakgof.korender.PixelFormat

internal fun Image.toArgbPixels(): IntArray {
    val pixels = IntArray(width * height)
    val raw = toRaw()

    when (format) {
        PixelFormat.RGB -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                val r = raw[src].toInt() and 0xFF
                val g = raw[src + 1].toInt() and 0xFF
                val b = raw[src + 2].toInt() and 0xFF
                pixels[dst++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                src += 3
            }
        }

        PixelFormat.RGBA -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                val r = raw[src].toInt() and 0xFF
                val g = raw[src + 1].toInt() and 0xFF
                val b = raw[src + 2].toInt() and 0xFF
                val a = raw[src + 3].toInt() and 0xFF
                pixels[dst++] = (a shl 24) or (r shl 16) or (g shl 8) or b
                src += 4
            }
        }

        PixelFormat.Gray -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                val gray = raw[src].toInt() and 0xFF
                pixels[dst++] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
                src += 1
            }
        }

        PixelFormat.Gray16 -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                val gray16 = ((raw[src].toInt() and 0xFF) shl 8) or (raw[src + 1].toInt() and 0xFF)
                val gray8 = ((gray16 * 255 + 32767) / 65535)
                pixels[dst++] = (0xFF shl 24) or (gray8 shl 16) or (gray8 shl 8) or gray8
                src += 2
            }
        }
    }

    return pixels
}

internal fun Image.toRgbaBytes(): ByteArray {
    val bytes = ByteArray(width * height * 4)
    val raw = toRaw()

    when (format) {
        PixelFormat.RGB -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                bytes[dst++] = raw[src]
                bytes[dst++] = raw[src + 1]
                bytes[dst++] = raw[src + 2]
                bytes[dst++] = -1
                src += 3
            }
        }

        PixelFormat.RGBA -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                bytes[dst++] = raw[src]
                bytes[dst++] = raw[src + 1]
                bytes[dst++] = raw[src + 2]
                bytes[dst++] = raw[src + 3]
                src += 4
            }
        }

        PixelFormat.Gray -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                val gray = raw[src]
                bytes[dst++] = gray
                bytes[dst++] = gray
                bytes[dst++] = gray
                bytes[dst++] = -1
                src += 1
            }
        }

        PixelFormat.Gray16 -> {
            var src = 0
            var dst = 0
            while (src < raw.size) {
                val gray16 = ((raw[src].toInt() and 0xFF) shl 8) or (raw[src + 1].toInt() and 0xFF)
                val gray8 = ((gray16 * 255 + 32767) / 65535).toByte()
                bytes[dst++] = gray8
                bytes[dst++] = gray8
                bytes[dst++] = gray8
                bytes[dst++] = -1
                src += 2
            }
        }
    }

    return bytes
}
