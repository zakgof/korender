package com.zakgof.korender.material

import com.zakgof.korender.declaration.MaterialDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.math.Color

object Materials {
    fun standard(vararg defs: String, block: StockUniforms.() -> Unit) =
        MaterialDeclaration(
            ShaderDeclaration("standard.vert", "standard.frag", setOf(*defs)),
            StockUniforms().apply(block)
        )
}

class StockUniforms : UniformSupplier {

    private val static = mutableMapOf<String, Any>()
    private val dynamic = mutableMapOf<String, () -> Any>()

    var colorTexture: GpuTexture? = null
    var normalTexture: GpuTexture? = null
    var aperiodicTexture: GpuTexture? = null
    var shadowTexture: GpuTexture? = null
    var detailTexture: GpuTexture? = null
    var colorFile: String? = null
    var normalFile: String? = null
    var aperiodicFile: String? = null
    var detailFile: String? = null
    var color = Color(0.5f, 0.5f, 0.5f);
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
            "color" -> color
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

    override fun boil(loader: (Any) -> Any) {
        if (colorFile != null) {
            colorTexture = loader(colorFile!!) as GpuTexture
        }
        if (normalFile != null) {
            normalTexture = loader(normalFile!!) as GpuTexture
        }
        if (aperiodicFile != null) {
            aperiodicTexture = loader(aperiodicFile!!) as GpuTexture
        }
        if (detailFile != null) {
            detailTexture = loader(detailFile!!) as GpuTexture
        }
    }

}