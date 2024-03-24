package com.zakgof.korender.impl.material

import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.declaration.TextureFilter
import com.zakgof.korender.declaration.TextureWrap
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.getPlatform

internal object Texturing {

    fun texture(textureResource: String): TextureDeclaration = TextureDeclaration(textureResource)

    fun create(textureFile: String): TextureBuilder =
        create(getPlatform().loadImage(Texturing::class.java.getResourceAsStream(textureFile)!!))

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
