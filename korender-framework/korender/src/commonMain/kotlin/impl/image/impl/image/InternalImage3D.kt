package com.zakgof.korender.impl.image.impl.image

import com.zakgof.korender.Image3D
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.math.ColorRGBA

internal class InternalImage3D(
    override val width: Int,
    override val height: Int,
    override val depth: Int,
    val bytes: NativeByteBuffer,
    override val format: PixelFormat
) : Image3D {

    override fun pixel(x: Int, y: Int, z: Int): ColorRGBA {
        val offset = x + y * width + z * width * height
        return when (format) {
            PixelFormat.RGB -> {
                val base = offset * 3
                ColorRGBA(
                    byteToFloat(bytes.byte(base)),
                    byteToFloat(bytes.byte(base + 1)),
                    byteToFloat(bytes.byte(base + 2)),
                    1.0f,
                )
            }

            PixelFormat.RGBA -> {
                val base = offset * 4
                ColorRGBA(
                    byteToFloat(bytes.byte(base)),
                    byteToFloat(bytes.byte(base + 1)),
                    byteToFloat(bytes.byte(base + 2)),
                    byteToFloat(bytes.byte(base + 3)),
                )
            }

            PixelFormat.Gray -> {
                val gray = byteToFloat(bytes.byte(offset))
                ColorRGBA.white(gray)
            }

            PixelFormat.Gray16 -> {
                val gray = (bytes.byte(offset * 2).toUByte().toFloat() * 256.0f +
                        bytes.byte((x + y * width) * 2 + 1).toUByte().toFloat()) / 65535.0f
                ColorRGBA.white(gray)
            }
        }
    }

    override fun setPixel(x: Int, y: Int, z: Int, color: ColorRGBA) {
        val offset = x + y * width + z * width * height
        when (format) {
            PixelFormat.RGB -> {
                bytes[offset * 3] = floatToByte(color.r)
                bytes[offset * 3 + 1] = floatToByte(color.g)
                bytes[offset * 3 + 2] = floatToByte(color.b)
            }

            PixelFormat.RGBA -> {
                bytes[offset * 4] = floatToByte(color.r)
                bytes[offset * 4 + 1] = floatToByte(color.g)
                bytes[offset * 4 + 2] = floatToByte(color.b)
                bytes[offset * 4 + 3] = floatToByte(color.a)
            }

            PixelFormat.Gray -> {
                bytes[offset] = floatToByte(color.r)
            }

            // TODO: Test
            PixelFormat.Gray16 -> {
                val w = floatToWord(color.r)
                bytes[offset * 2] = (w % 255).toByte()
                bytes[offset * 2 + 1] = (w / 256).toByte()
            }
        }
    }

    fun byteToFloat(byte: Byte): Float = byte.toUByte().toFloat() / 255.0f

    fun floatToByte(float: Float): Byte = (float * 255f).toUInt().toByte()

    fun floatToWord(float: Float): Short = (float * 65535).toUInt().toShort()

    override fun toRaw(): ByteArray = ByteArray(bytes.size()) { bytes.byte(it) }

}