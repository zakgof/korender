package com.zakgof.korender.impl.material

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BaseParams
import com.zakgof.korender.BillboardVertexParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.StandartParams.Pbr
import com.zakgof.korender.StandartParams.SpecularGlossiness
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.WaterParams
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat4List

typealias DynamicUniforms = () -> Map<String, Any?>

internal abstract class InternalBaseParams : BaseParams {

    val map = mutableMapOf<String, Any?>()
    val defs = mutableSetOf<String>()

    override fun set(key: String, value: Any) {
        map[key] = value
    }

    abstract fun collect()
}

internal class InternalBlurParams : BlurParams, InternalBaseParams() {

    override var radius = 1.0f

    override fun collect() {
        map["radius"] = radius
    }
}

internal class InternalAdjustParams : AdjustParams, InternalBaseParams() {

    override var brightness: Float = 0f;
    override var contrast: Float = 1f
    override var saturation: Float = 1f

    override fun collect() {
        map["brightness"] = brightness
        map["contrast"] = contrast
        map["saturation"] = saturation
    }
}

internal open class InternalBillboardVertexParams : BillboardVertexParams, InternalBaseParams() {

    override var xscale: Float = 1.0f
    override var yscale: Float = 1.0f
    override var rotation: Float = 0.0f

    override fun collect() {
        map["xscale"] = xscale
        map["yscale"] = yscale
        map["rotation"] = rotation
    }
}

internal class InternalFireballParams : FireballParams, InternalBillboardVertexParams() {

    override var power = 0.5f

    override fun collect() {
        map["power"] = power
        super.collect()
    }
}

internal class InternalFireParams : FireParams, InternalBillboardVertexParams() {

    override var strength = 3.0f

    override fun collect() {
        map["strength"] = strength
        super.collect()
    }
}

internal class InternalSmokeParams : SmokeParams, InternalBillboardVertexParams() {

    override var density = 0.5f
    override var seed = 0f

    override fun collect() {
        map["density"] = density
        map["seed"] = seed
        super.collect()
    }
}

internal class InternalWaterParams : WaterParams, InternalBaseParams() {

    override var waterColor: Color = Color(1.0f, 0.1f, 0.2f, 0.3f)
    override var transparency: Float = 0.1f
    override var waveScale: Float = 0.04f

    override fun collect() {
        map["waterColor"] = waterColor
        map["transparency"] = transparency
        map["waveScale"] = waveScale
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

    override fun collect() {
        map["density"] = density
        map["thickness"] = thickness
        map["scale"] = scale
        map["darkblue"] = darkblue
        map["lightblue"] = lightblue
        map["rippleamount"] = rippleamount
        map["ripplescale"] = ripplescale
    }
}

internal class InternalStandartParams : StandartParams, InternalBaseParams() {

    private var _pbr: Pbr? = null
    private var _specularGlossiness: SpecularGlossiness? = null

    override var pcss = false

    override var baseColor = Color(1f, 0.5f, 0.5f, 0.5f)
    override var baseColorTexture: TextureDeclaration? = null

    override val pbr: Pbr
        get() {
            if (_pbr == null) {
                _pbr = InternalPbr()
            }
            return _pbr!!
        }
    override val specularGlossiness: SpecularGlossiness
        get() {
            if (_specularGlossiness == null) {
                _specularGlossiness = InternalSpecularGlossiness()
            }
            return _specularGlossiness!!
        }

    override var normalTexture: TextureDeclaration? = null
    override var shadowTexture: TextureDeclaration? = null

    override var jointMatrices: Mat4List? = null
    override var inverseBindMatrices: Mat4List? = null

    override var xscale = 1f
    override var yscale = 1f
    override var rotation = 0f

    override fun collect() {

        // TODO ALBEDO map should be available without shading model

        map["baseColor"] = baseColor
        map["baseColorTexture"] = baseColorTexture

        if (_pbr != null) {
            map["metallic"] = _pbr!!.metallic
            map["roughness"] = _pbr!!.roughness
            map["emissiveFactor"] = _pbr!!.emissiveFactor
            map["metallicRoughnessTexture"] = _pbr!!.metallicRoughnessTexture
            map["emissiveTexture"] = _pbr!!.emissiveTexture
            map["occlusionTexture"] = _pbr!!.occlusionTexture
            defs += "PBR"
            _pbr!!.metallicRoughnessTexture?.let { defs += "METALLIC_ROUGHNESS_MAP" }
            _pbr!!.emissiveTexture?.let { defs += "EMISSIVE_MAP" }
            _pbr!!.occlusionTexture?.let { defs += "OCCLUSION_MAP" }
        }

        if (_specularGlossiness != null) {
            map["diffuseFactor"] = _specularGlossiness!!.diffuseFactor
            map["diffuseTexture"] = _specularGlossiness!!.diffuseTexture
            map["specularFactor"] = _specularGlossiness!!.specularFactor
            map["glossinessFactor"] = _specularGlossiness!!.glossinessFactor
            map["specularGlossinessTexture"] = _specularGlossiness!!.specularGlossinessTexture
            defs += "SPECULAR_GLOSSINESS"

            _specularGlossiness!!.diffuseTexture?.let { defs += "DIFFUSE_MAP" }
            _specularGlossiness!!.specularGlossinessTexture?.let { defs += "SPECULAR_GLOSSINESS_MAP" }
        }

        map["normalTexture"] = normalTexture
        map["shadowTexture"] = shadowTexture

        map["jointMatrices[0]"] = jointMatrices
        map["inverseBindMatrices[0]"] = inverseBindMatrices

        map["xscale"] = xscale
        map["yscale"] = yscale
        map["rotation"] = rotation

        baseColorTexture?.let { defs += "BASE_COLOR_MAP" }
        normalTexture?.let { defs += "NORMAL_MAP" }
        jointMatrices?.let { defs += "SKINNING" }

        if (_pbr == null && _specularGlossiness == null) {
            defs += "NO_LIGHT"
        }
        if (pcss) {
            defs += "PCSS"
        }
    }

    internal class InternalPbr : Pbr {
        override var metallic = 0.3f
        override var roughness = 0.3f
        override var emissiveFactor = Color(1f, 1f, 1f, 1f)
        override var metallicRoughnessTexture: TextureDeclaration? = null
        override var emissiveTexture: TextureDeclaration? = null
        override var occlusionTexture: TextureDeclaration? = null
    }

    internal class InternalSpecularGlossiness : SpecularGlossiness {
        override var diffuseFactor: Color = Color.White
        override var diffuseTexture: TextureDeclaration? = null
        override var specularFactor: Color = Color.White
        override var glossinessFactor: Float = 0.2f
        override var specularGlossinessTexture: TextureDeclaration? = null
    }
}

internal class ParamUniforms<P : InternalBaseParams>(
    private val params: P,
    private val block: P.() -> Unit
) : DynamicUniforms {

    override fun invoke(): Map<String, Any?> {
        block.invoke(params)
        params.collect()
        return params.map
    }

    fun shaderDefs(): Set<String> {
        block.invoke(params)
        params.collect()
        return params.defs
    }
}