package com.zakgof.korender.impl.material

import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BaseParams
import com.zakgof.korender.BillboardVertexParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.WaterParams
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat4

internal abstract class InternalBaseParams : BaseParams {

    private val map = mutableMapOf<String, Any>()

    override fun set(key: String, value: Any) {
        map[key] = value
    }

    open operator fun get(key: String): Any? = map[key]
}

internal class InternalBlurParams : BlurParams, InternalBaseParams() {

    override var radius = 1.0f

    override operator fun get(key: String): Any? = when (key) {
        "radius" -> radius
        else -> super.get(key)
    }
}

internal class InternalAdjustParams : AdjustParams, InternalBaseParams() {

    override var brightness: Float = 0f;
    override var contrast: Float = 1f
    override var saturation: Float = 1f

    override operator fun get(key: String): Any? = when (key) {
        "brightness" -> brightness
        "contrast" -> contrast
        "saturation" -> saturation
        else -> super.get(key)
    }
}

internal open class InternalBillboardVertexParams : BillboardVertexParams, InternalBaseParams() {

    override var xscale: Float = 1.0f
    override var yscale: Float = 1.0f
    override var rotation: Float = 0.0f

    override operator fun get(key: String): Any? = when (key) {
        "xscale" -> xscale
        "yscale" -> yscale
        "rotation" -> rotation
        else -> super.get(key)
    }
}

internal class InternalFireballParams : FireballParams, InternalBillboardVertexParams() {

    override var power = 0.5f

    override operator fun get(key: String): Any? = when (key) {
        "power" -> power
        else -> super.get(key)
    }
}

internal class InternalFireParams : FireParams, InternalBillboardVertexParams() {

    override var strength = 3.0f

    override operator fun get(key: String): Any? = when (key) {
        "strength" -> strength
        else -> super.get(key)
    }
}

internal class InternalSmokeParams : SmokeParams, InternalBillboardVertexParams() {

    override var density = 0.5f
    override var seed = 0f

    override operator fun get(key: String): Any? = when (key) {
        "density" -> density
        "seed" -> seed
        else -> super.get(key)
    }
}

internal class InternalWaterParams : WaterParams, InternalBaseParams() {

    override var waterColor: Color = Color(1.0f, 0.1f, 0.2f, 0.3f)
    override var transparency: Float = 0.1f
    override var waveScale: Float = 0.04f

    override operator fun get(key: String): Any? = when (key) {
        "waterColor" -> waterColor
        "transparency" -> transparency
        "waveScale" -> waveScale
        else -> super.get(key)
    }
}

internal class InternalFastCloudSkyParams : FastCloudSkyParams, InternalBaseParams() {

    override var density = 3.0f     // 0..5
    override var thickness = 10.0f  // 0..20
    override var scale = 1.0f       // 0.1..10
    override var rippleamount = 0.3f  // 0..1
    override var ripplescale = 4.0f  // 1..10
    override var darkblue = Color(1f, 0.2f, 0.4f, 0.6f)
    override var lightblue = Color(1f, 0.4f, 0.6f, 1.0f)

    override operator fun get(key: String): Any? = when (key) {
        "density" -> density
        "thickness" -> thickness
        "scale" -> scale
        "darkblue" -> darkblue
        "lightblue" -> lightblue
        "rippleamount" -> rippleamount
        "ripplescale" -> ripplescale
        else -> super.get(key)
    }
}

internal class InternalStandartParams : StandartParams, InternalBaseParams() {

    override var baseColor = Color(1f, 0.5f, 0.5f, 0.5f)
    override var metallic = 0.3f
    override var roughness = 0.3f
    override var emissiveFactor = Color(1f, 1f, 1f, 1f)

    override var albedoTexture: TextureDeclaration? = null
    override var metallicRoughnessTexture: TextureDeclaration? = null
    override var emissiveTexture: TextureDeclaration? = null
    override var occlusionTexture: TextureDeclaration? = null

    override var normalTexture: TextureDeclaration? = null
    override var shadowTexture: TextureDeclaration? = null

    override var jointMatrices: List<Mat4>? = null
    override var inverseBindMatrices: List<Mat4>? = null

    override var xscale = 1f
    override var yscale = 1f
    override var rotation = 0f

    override operator fun get(key: String): Any? = when (key) {
        "baseColor" -> baseColor
        "metallic" -> metallic
        "roughness" -> roughness
        "emissiveFactor" -> emissiveFactor

        "albedoTexture" -> albedoTexture
        "metallicRoughnessTexture" -> metallicRoughnessTexture
        "emissiveTexture" -> emissiveTexture
        "occlusionTexture" -> occlusionTexture

        "normalTexture" -> normalTexture
        "shadowTexture" -> shadowTexture

        "jointMatrices[0]" -> jointMatrices
        "inverseBindMatrices[0]" -> inverseBindMatrices

        "xscale" -> xscale
        "yscale" -> yscale
        "rotation" -> rotation

        else -> super.get(key)
    }
}

internal class BaseParamUniforms<P : BaseParams>(private val params: P, private val block: P.() -> Unit) : UniformSupplier {

    init {
        update()
    }

    override fun update() {
        params.apply(block)
    }

    override fun get(key: String): Any? =
        (params as InternalBaseParams)[key]
}