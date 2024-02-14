package com.zakgof.korender.material

import com.zakgof.korender.Gpu
import com.zakgof.korender.gpu.GpuTexture

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
    var aperiodicFile: String? = null
    var aperiodicMap: GpuTexture? = null
    var ambient = 1f
    var diffuse = 1f
    var specular = 1f
    var specularPower = 20f

    fun build(gpu: Gpu): UniformSupplier {

        if (aperiodicFile != null) {
            aperiodicMap = Textures.create(aperiodicFile!!)
                .filter(TextureFilter.Nearest)
                .build(gpu)
        }
        if (textureFile != null) {
            textureMap = Textures.create(textureFile!!)
                .build(gpu)
        }
        return MapUniformSupplier(
            Pair("textureMap", textureMap),
            Pair("tileIndexMap", aperiodicMap),
            Pair("triplanarScale", triplanarScale),
            Pair("ambient", ambient),
            Pair("diffuse", diffuse),
            Pair("specular", specular),
            Pair("specularPower", specularPower)
        )
    }
}