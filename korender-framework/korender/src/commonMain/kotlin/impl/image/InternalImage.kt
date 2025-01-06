package com.zakgof.korender.impl.image

import com.zakgof.korender.Image
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.Color

internal interface InternalImage : Image {

    val bytes: NativeByteBuffer

    override fun pixel(x: Int, y: Int): Color =
        when (format) {
            Image.Format.RGB -> Color(
                1.0f,
                byteToFloat(bytes.byte((x + y * width) * 3)),
                byteToFloat(bytes.byte((x + y * width) * 3 + 1)),
                byteToFloat(bytes.byte((x + y * width) * 3 + 2))
            )

            Image.Format.RGBA -> Color(
                byteToFloat(bytes.byte((x + y * width) * 4 + 3)),
                byteToFloat(bytes.byte((x + y * width) * 4)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 1)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 2))
            )

            Image.Format.Gray -> {
                val gray = byteToFloat(bytes.byte((x + y * width)))
                Color(1.0f, gray, gray, gray)
            }

            Image.Format.Gray16 -> {
                val gray = (bytes.byte((x + y * width) * 2).toUByte().toFloat() * 256.0f +
                        bytes.byte((x + y * width) * 2 + 1).toUByte().toFloat()) / 65535.0f
                Color(1.0f, gray, gray, gray)
            }
        }

    fun byteToFloat(byte: Byte): Float = byte.toUByte().toFloat() / 255.0f

}