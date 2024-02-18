package com.zakgof.korender.material

import com.zakgof.korender.Gpu
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.math.Color

object Materials {

    fun create(gpuShader: GpuShader, uniforms: UniformSupplier = UniformSupplier { null }) = object : Material {
        override val gpuShader
            get() = gpuShader
        override val uniforms: UniformSupplier
            get() = uniforms
    }

    fun standard(gpu: Gpu, vararg defs: String, block: StandardMaterial.() -> Unit): StandardMaterial =
        StandardMaterial(gpu, *defs).apply(block)

    fun sky(gpu: Gpu, skyTextureFile: String) = create(
        ShaderBuilder("screen.vert", "texsky.frag").build(gpu),
        MapUniformSupplier("skyTexture" to Textures.create(skyTextureFile).build(gpu))
    )

    fun text(gpu: Gpu, fontTexture: GpuTexture, color: Color) = create(
        ShaderBuilder("font.vert", "font.frag").build(gpu),
        MapUniformSupplier("fontTexture" to fontTexture, "color" to color)
    )

}

class StandardMaterial(private val gpu: Gpu, vararg defs: String) : Material {

    override val gpuShader: GpuShader = Shaders.standard(gpu, *defs)
    override val uniforms: UniformSupplier
        get() = UniformSupplier { get(it) }

    var colorTexture: GpuTexture? = null
    var normalTexture: GpuTexture? = null
    var aperiodicTexture: GpuTexture? = null
    var shadowTexture: GpuTexture? = null
    var colorFile: String? = null
        set(value) {
            field = value
            colorTexture = Textures.create(colorFile!!)
                .build(gpu)
        }
    var normalFile: String? = null
        set(value) {
            field = value
            normalTexture = Textures.create(normalFile!!)
                .build(gpu)
        }
    var aperiodicFile: String? = null
        set(value) {
            field = value
            aperiodicTexture = Textures.create(aperiodicFile!!)
                .filter(TextureFilter.Nearest)
                .build(gpu)
        }
    var triplanarScale = 1.0f
    var ambient = 0.3f
    var diffuse = 0.7f
    var specular = 0.3f
    var specularPower = 20f

    fun get(key: String): Any? =
        when (key) {
            "colorTexture" -> colorTexture
            "normalTexture" -> normalTexture
            "aperiodicTexture" -> aperiodicTexture
            "shadowTexture" -> shadowTexture
            "triplanarScale" -> triplanarScale
            "ambient" -> ambient
            "diffuse" -> diffuse
            "specular" -> specular
            "specularPower" -> specularPower
            else -> null
        }
}