package com.zakgof.korender.material

import com.zakgof.korender.Gpu
import com.zakgof.korender.KorenderException
import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.glgpu.GlGpuTexture
import com.zakgof.korender.gpu.GpuTexture
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object Textures {

    fun create(resource: String): TextureBuilder = create(ImageIO.read(Textures::class.java.getResourceAsStream(resource)!!))
    fun create(image: BufferedImage): TextureBuilder = TextureBuilder(image)

    class TextureBuilder(private val image: BufferedImage) {

        var filter: TextureFilter = TextureFilter.MipMapLinearLinear

        fun build(gpu: Gpu): GpuTexture {
            val bytes = when (image.type) {
                BufferedImage.TYPE_3BYTE_BGR -> loadBgr((image.raster.dataBuffer as DataBufferByte).data)
                else -> throw KorenderException("Unknown image format ${image.type}")
            }
            return GlGpuTexture(image.width, image.height, bytes, filter)
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

        fun filter(filter: TextureFilter): TextureBuilder {
            this.filter = filter
            return this
        }

    }


}
