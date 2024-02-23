package com.zakgof.korender.material

import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.math.Color

object Materials {

    fun create(gpuShader: GpuShader, uniforms: UniformSupplier = UniformSupplier { null }) =
        DirectMaterial(gpuShader, uniforms)

    class DirectMaterial(override val gpuShader: GpuShader, override val uniforms: UniformSupplier) : Material

    fun standard(gpu: Gpu, vararg defs: String, block: StockUniforms.() -> Unit): Material = create(
        Shaders.standard(gpu, *defs),
        StockUniforms(gpu).apply(block)
    )

    fun sky(gpu: Gpu, skyTextureFile: String) = create(
        Shaders.create(gpu, "screen.vert", "texsky.frag"),
        MapUniformSupplier("skyTexture" to Textures.create(skyTextureFile).build(gpu))
    )

    fun text(gpu: Gpu, fontTexture: GpuTexture, color: Color) = create(
        Shaders.create(gpu, "font.vert", "font.frag"),
        MapUniformSupplier("fontTexture" to fontTexture, "color" to color)
    )

    fun billboard(gpu: Gpu, block: StockUniforms.() -> Unit): Material = create(
        Shaders.create(gpu,"billboard.vert", "standard.frag"),
        StockUniforms(gpu).apply(block)
    )

}

class StockUniforms(private val gpu: Gpu) : UniformSupplier {

    private val static = mutableMapOf<String, Any>()
    private val dynamic = mutableMapOf<String, () -> Any>()

    var colorTexture: GpuTexture? = null
    var normalTexture: GpuTexture? = null
    var aperiodicTexture: GpuTexture? = null
    var shadowTexture: GpuTexture? = null
    var detailTexture: GpuTexture? = null
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
    var detailFile: String? = null
        set(value) {
            field = value
            detailTexture = Textures.create(detailFile!!)
                .build(gpu)
        }
    var triplanarScale = 1.0f
    var detailScale = 16.0f
    var detailRatio = 0.3f
    var ambient = 0.3f
    var diffuse = 0.7f
    var specular = 0.3f
    var specularPower = 20f
    var xscale = 1f
    var yscale = 1f
    var rotation = 0f

    fun static(key: String, value: Any) {
        static[key] = value
    }

    fun dynamic(key: String, valueSupplier: () -> Any) {
        dynamic[key] = valueSupplier
    }

    override operator fun get(key: String): Any? =
        when (key) {
            "colorTexture" -> colorTexture
            "normalTexture" -> normalTexture
            "aperiodicTexture" -> aperiodicTexture
            "shadowTexture" -> shadowTexture
            "detailTexture" -> detailTexture
            "triplanarScale" -> triplanarScale
            "detailScale" -> detailScale
            "detailRatio" -> detailRatio
            "ambient" -> ambient
            "diffuse" -> diffuse
            "specular" -> specular
            "specularPower" -> specularPower
            "xscale" -> xscale
            "yscale" -> yscale
            "rotation" -> rotation
            else -> static[key] ?: dynamic[key]?.let { it() }
        }

}