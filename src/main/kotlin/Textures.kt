package com.zakgof.korender

import com.zakgof.korender.glgpu.BufferUtils
import com.zakgof.korender.glgpu.GlGpuTexture
import com.zakgof.korender.gpu.GpuTexture
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

object Textures {
    fun create(image: BufferedImage): TextureBuilder = TextureBuilder(image)

    class TextureBuilder(private val image: BufferedImage) {
        fun build(gpu: Gpu): GpuTexture {
            val data = (image.raster.dataBuffer as DataBufferByte).data
            val bytes = BufferUtils.createByteBuffer(data.size)
                .put(data, 0, data.size)
                .flip()
            return GlGpuTexture(image.width, image.height, bytes)
        }

    }


}
