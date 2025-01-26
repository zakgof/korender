package com.zakgof.korender.impl.material

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BaseParams
import com.zakgof.korender.BillboardVertexParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.FogParams
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.StandartParams.Pbr
import com.zakgof.korender.StandartParams.SpecularGlossiness
import com.zakgof.korender.StarrySkyParams
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.WaterParams
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.white

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

internal class InternalStarrySkyParams : StarrySkyParams, InternalBaseParams() {

    override var colorness = 0.8f;
    override var density = 20.0f;
    override var speed = 1.0f;
    override var size = 15.0f;

    override fun collect() {
        map["colorness"] = colorness
        map["density"] = density
        map["speed"] = speed
        map["size"] = size
    }
}

internal class InternalStandartParams : StandartParams, InternalBaseParams() {

    private var _specularGlossiness: SpecularGlossiness? = null

    override var pcss = false

    override var baseColor = white(1.0f)
    override var emissiveFactor = white(1.0f)
    override var baseColorTexture: TextureDeclaration? = null
    override var triplanarScale: Float? = null

    override var normalTexture: TextureDeclaration? = null
    override var shadowTexture: TextureDeclaration? = null
    override var emissiveTexture: TextureDeclaration? = null

    override val pbr = InternalPbr()

    override val specularGlossiness: SpecularGlossiness
        get() {
            if (_specularGlossiness == null) {
                _specularGlossiness = InternalSpecularGlossiness()
            }
            return _specularGlossiness!!
        }

    var jntMatrices: Mat4List? = null

    override var xscale = 1f
    override var yscale = 1f
    override var rotation = 0f

    override fun collect() {

        map["baseColor"] = baseColor
        map["baseColorTexture"] = baseColorTexture

        map["metallic"] = pbr.metallic
        map["roughness"] = pbr.roughness
        map["metallicRoughnessTexture"] = pbr.metallicRoughnessTexture

        map["emissiveFactor"] = emissiveFactor
        map["emissiveTexture"] = emissiveTexture

//            map["occlusionTexture"] = _pbr!!.occlusionTexture

        pbr.metallicRoughnessTexture?.let { defs += "METALLIC_ROUGHNESS_MAP" }
//          _pbr!!.occlusionTexture?.let { defs += "OCCLUSION_MAP" }

        if (_specularGlossiness != null) {
            map["specularFactor"] = _specularGlossiness!!.specularFactor
            map["glossinessFactor"] = _specularGlossiness!!.glossinessFactor
            map["specularGlossinessTexture"] = _specularGlossiness!!.specularGlossinessTexture
            defs += "SPECULAR_GLOSSINESS"
            _specularGlossiness!!.specularGlossinessTexture?.let { defs += "SPECULAR_GLOSSINESS_MAP" }
        }

        map["normalTexture"] = normalTexture
        map["shadowTexture"] = shadowTexture
        map["triplanarScale"] = triplanarScale

        map["jntMatrices[0]"] = jntMatrices

        map["xscale"] = xscale
        map["yscale"] = yscale
        map["rotation"] = rotation

        baseColorTexture?.let { defs += "BASE_COLOR_MAP" }
        normalTexture?.let { defs += "NORMAL_MAP" }
        emissiveTexture?.let { defs += "EMISSIVE_MAP" }

        jntMatrices?.let { defs += "SKINNING" }
        if (pcss) {
            defs += "PCSS"
        }
        triplanarScale?.let { defs += "TRIPLANAR" }
    }

    internal class InternalPbr : Pbr {
        override var metallic = 0.1f
        override var roughness = 0.5f
        override var metallicRoughnessTexture: TextureDeclaration? = null

//        override var emissiveFactor = Color(1f, 1f, 1f, 1f)
//        override var emissiveTexture: TextureDeclaration? = null
//        override var occlusionTexture: TextureDeclaration? = null

    }

    internal class InternalSpecularGlossiness : SpecularGlossiness {
        override var specularFactor: Color = Color.White
        override var glossinessFactor: Float = 0.2f
        override var specularGlossinessTexture: TextureDeclaration? = null
    }
}

internal class InternalFogParams : FogParams, InternalBaseParams() {

    override var density = 0.02f
    override var color = white(0.01f)

    override fun collect() {
        map["density"] = density
        map["fogColor"] = color
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