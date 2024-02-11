package com.zakgof.korender.material

import com.zakgof.korender.Gpu
import com.zakgof.korender.gpu.GpuTexture
import javax.imageio.ImageIO

object Materials {
    fun standard(gpu: Gpu, vararg defs: String, block: StandardUniforms.() -> Unit): GpuMaterial {
        val gpuShader = ShaderBuilder("test.vert", "test.frag", *defs).build(gpu)
        val uniformSupplier = StandardUniforms().apply(block).build(gpu)
        return GpuMaterial(gpuShader, uniformSupplier)
    }
}

class StandardUniforms {
    var triplanarScale: Float = 1.0f
    var textureFile: String? = null
    var textureMap: GpuTexture? = null

    fun build(gpu: Gpu): UniformSupplier {

        if (textureFile != null) {
            textureMap = Textures.create(ImageIO.read(Textures::class.java.getResourceAsStream(textureFile))).build(gpu)
        }
        return MapUniformSupplier(
            Pair("textureMap", textureMap!!),
            Pair("triplanarScale", triplanarScale)
        )
    }
}