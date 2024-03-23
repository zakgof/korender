package com.zakgof.korender.material

import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.getPlatform
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuTexture

object Textures {

    fun texture(textureResource: String): TextureDeclaration = TextureDeclaration(textureResource)

    fun create(textureFile: String): TextureBuilder =
        create(getPlatform().loadImage(Textures::class.java.getResourceAsStream(textureFile)!!))

    fun create(image: Image): TextureBuilder = TextureBuilder(image)

    class TextureBuilder(private val image: Image) {

        var filter: TextureFilter = TextureFilter.MipMapLinearLinear
        var wrap: TextureWrap = TextureWrap.Repeat
        var aniso: Int = 1024

        fun build(gpu: Gpu): GpuTexture {
            val bytes = image.bytes
            return gpu.createTexture(
                image.width,
                image.height,
                bytes,
                filter,
                wrap,
                aniso,
                image.format
            )
        }

        fun filter(filter: TextureFilter): TextureBuilder {
            this.filter = filter
            return this
        }

    }


}
