package com.zakgof.korender.impl.image

import com.zakgof.korender.PixelFormat
import com.zakgof.korender.impl.buffer.NativeByteBuffer

object Tga {

    fun encode(width: Int, height: Int, format: PixelFormat, bytes: NativeByteBuffer): ByteArray {

        val header = ByteArray(18)
        header[2] = 2 // Uncompressed RGB
        header[12] = (width and 0xFF).toByte()
        header[13] = ((width shr 8) and 0xFF).toByte()
        header[14] = (height and 0xFF).toByte()
        header[15] = ((height shr 8) and 0xFF).toByte()
        header[16] = 32 // 32 bits per pixel
        header[17] = 0x20 // Origin top-left (optional: 0x00 for bottom-left)

        val image = ByteArray(width * height * 4)

        var src = 0
        var dst = 0
        while (src < bytes.size()) {
            val r = bytes.byte(src)
            val g = bytes.byte(src + 1)
            val b = bytes.byte(src + 2)
            val a = bytes.byte(src + 3)
            image[dst] = b
            image[dst + 1] = g
            image[dst + 2] = r
            image[dst + 3] = a
            src += 4
            dst += 4
        }

        return header + image
    }
}