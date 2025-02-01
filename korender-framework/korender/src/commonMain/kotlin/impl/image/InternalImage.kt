package com.zakgof.korender.impl.image

import com.zakgof.korender.Image
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.ColorRGBA

internal interface InternalImage : Image {

    val bytes: NativeByteBuffer

    override fun pixel(x: Int, y: Int): ColorRGBA =
        when (format) {
            Image.Format.RGB -> ColorRGBA(
                byteToFloat(bytes.byte((x + y * width) * 3)),
                byteToFloat(bytes.byte((x + y * width) * 3 + 1)),
                byteToFloat(bytes.byte((x + y * width) * 3 + 2)),
                1.0f,
            )

            Image.Format.RGBA -> ColorRGBA(
                byteToFloat(bytes.byte((x + y * width) * 4)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 1)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 2)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 3)),
            )

            Image.Format.Gray -> {
                val gray = byteToFloat(bytes.byte((x + y * width)))
                ColorRGBA.white(gray)
            }

            Image.Format.Gray16 -> {
                val gray = (bytes.byte((x + y * width) * 2).toUByte().toFloat() * 256.0f +
                        bytes.byte((x + y * width) * 2 + 1).toUByte().toFloat()) / 65535.0f
                ColorRGBA.white(gray)
            }
        }

    fun byteToFloat(byte: Byte): Float = byte.toUByte().toFloat() / 255.0f

}