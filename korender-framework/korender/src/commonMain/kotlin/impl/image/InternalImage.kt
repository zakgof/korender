package com.zakgof.korender.impl.image

import com.zakgof.korender.Image
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.ColorRGBA

internal class InternalImage(
    override val width: Int,
    override val height: Int,
    val bytes: NativeByteBuffer,
    override val format: PixelFormat
) : Image {

    override fun pixel(x: Int, y: Int): ColorRGBA =
        when (format) {
            PixelFormat.RGB -> ColorRGBA(
                byteToFloat(bytes.byte((x + y * width) * 3)),
                byteToFloat(bytes.byte((x + y * width) * 3 + 1)),
                byteToFloat(bytes.byte((x + y * width) * 3 + 2)),
                1.0f,
            )

            PixelFormat.RGBA -> ColorRGBA(
                byteToFloat(bytes.byte((x + y * width) * 4)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 1)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 2)),
                byteToFloat(bytes.byte((x + y * width) * 4 + 3)),
            )

            PixelFormat.Gray -> {
                val gray = byteToFloat(bytes.byte((x + y * width)))
                ColorRGBA.white(gray)
            }

            PixelFormat.Gray16 -> {
                val gray = (bytes.byte((x + y * width) * 2).toUByte().toFloat() * 256.0f +
                        bytes.byte((x + y * width) * 2 + 1).toUByte().toFloat()) / 65535.0f
                ColorRGBA.white(gray)
            }
        }

    override fun setPixel(x: Int, y: Int, color: ColorRGBA) {
        when (format) {
            PixelFormat.RGB -> {
                bytes[(x + y * width) * 3] = floatToByte(color.r)
                bytes[(x + y * width) * 3 + 1] = floatToByte(color.g)
                bytes[(x + y * width) * 3 + 2] = floatToByte(color.b)
            }

            PixelFormat.RGBA -> {
                bytes[(x + y * width) * 4] = floatToByte(color.r)
                bytes[(x + y * width) * 4 + 1] = floatToByte(color.g)
                bytes[(x + y * width) * 4 + 2] = floatToByte(color.b)
                bytes[(x + y * width) * 4 + 3] = floatToByte(color.a)
            }

            PixelFormat.Gray -> {
                bytes[x + y * width] = floatToByte(color.r)
            }

            // TODO: Test
            PixelFormat.Gray16 -> {
                val w = floatToWord(color.r)
                bytes[(x + y * width) * 2] = (w % 255).toByte()
                bytes[(x + y * width) * 2 + 1] = (w / 256).toByte()
            }
        }
    }

    fun byteToFloat(byte: Byte): Float = byte.toUByte().toFloat() / 255.0f

    fun floatToByte(float: Float): Byte = (float * 255f).toUInt().toByte()

    fun floatToWord(float: Float): Short = (float * 65535).toUInt().toShort()

    override fun toTga(): ByteArray = Tga.encode(width, height, format, bytes)

    override fun toRaw(): ByteArray = ByteArray(bytes.size()) { bytes.byte(it) }

}