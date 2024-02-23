package com.zakgof.korender.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuTexture
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object Textures {

    fun create(textureFile: String): TextureBuilder =
        create(ImageIO.read(Textures::class.java.getResourceAsStream(textureFile)!!))

    fun create(image: BufferedImage): TextureBuilder = TextureBuilder(image)

    class TextureBuilder(private val image: BufferedImage) {

        var filter: TextureFilter = TextureFilter.MipMapLinearLinear
        var wrap: TextureWrap = TextureWrap.Repeat
        var aniso: Int = 1024

        fun build(gpu: Gpu): GpuTexture {
            val bytes = when (image.type) {
                BufferedImage.TYPE_3BYTE_BGR -> loadBgr((image.raster.dataBuffer as DataBufferByte).data)
                BufferedImage.TYPE_4BYTE_ABGR -> loadAbgr((image.raster.dataBuffer as DataBufferByte).data)
                BufferedImage.TYPE_BYTE_GRAY -> loadGray((image.raster.dataBuffer as DataBufferByte).data)
                else -> throw KorenderException("Unknown image format ${image.type}")
            }
            return gpu.createTexture(
                image.width,
                image.height,
                bytes,
                filter,
                wrap,
                aniso,
                when (image.type) {
                    BufferedImage.TYPE_3BYTE_BGR -> GpuTexture.Format.RGB
                    BufferedImage.TYPE_4BYTE_ABGR -> GpuTexture.Format.RGBA
                    else -> throw KorenderException("Unknown image format ${image.type}")
                }
            )
        }

        private fun loadBgr(data: ByteArray): ByteBuffer {
            val buffer = BufferUtils.createByteBuffer(data.size)
            for (i in 0 until data.size / 3) {
                buffer.put(data[i * 3 + 2])
                    .put(data[i * 3 + 1])
                    .put(data[i * 3])
            }
            return buffer.flip();
        }

        private fun loadGray(data: ByteArray): ByteBuffer {
            val buffer = BufferUtils.createByteBuffer(data.size)
            buffer.put(data)
            return buffer.flip()
        }

        private fun loadAbgr(data: ByteArray): ByteBuffer {
            val buffer = BufferUtils.createByteBuffer(data.size)
            for (i in 0 until data.size / 4) {
                buffer.put(data[i * 4 + 3])
                    .put(data[i * 4 + 2])
                    .put(data[i * 4 + 1])
                    .put(data[i * 4])
//                buffer.put(255.toByte()) // r
//                    .put(128.toByte())   // g
//                    .put(128.toByte())   // b
//                    .put(16.toByte())   // a
            }
            return buffer.flip();
        }

        fun filter(filter: TextureFilter): TextureBuilder {
            this.filter = filter
            return this
        }

    }


}
