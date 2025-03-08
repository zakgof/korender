package com.zakgof.korender.impl.material

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BaseParams
import com.zakgof.korender.BillboardVertexParams
import com.zakgof.korender.BloomParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.FogParams
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.SsrParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.StandartParams.Pbr
import com.zakgof.korender.StandartParams.SpecularGlossiness
import com.zakgof.korender.StarrySkyParams
import com.zakgof.korender.TerrainParams
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.WaterParams
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec3

internal open class InternalBaseParams : BaseParams {

    val map = mutableMapOf<String, Any?>()

    override fun set(key: String, value: Any) {
        map[key] = value
    }

    open fun collect(mb: MaterialBuilder) {
        mb.uniforms += map
    }
}

internal class InternalBlurParams : BlurParams, InternalBaseParams() {

    override var radius = 1.0f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["radius"] = radius
        super.collect(mb)
    }
}

internal class InternalAdjustParams : AdjustParams, InternalBaseParams() {

    override var brightness: Float = 0f;
    override var contrast: Float = 1f
    override var saturation: Float = 1f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["brightness"] = brightness
        mb.uniforms["contrast"] = contrast
        mb.uniforms["saturation"] = saturation
        super.collect(mb)
    }
}

internal open class InternalBillboardVertexParams : BillboardVertexParams, InternalBaseParams() {

    override var xscale: Float = 1.0f
    override var yscale: Float = 1.0f
    override var rotation: Float = 0.0f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["xscale"] = xscale
        mb.uniforms["yscale"] = yscale
        mb.uniforms["rotation"] = rotation
        super.collect(mb)
    }
}

internal class InternalFireballParams : FireballParams, InternalBillboardVertexParams() {

    override var power = 0.5f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["power"] = power
        super.collect(mb)
    }
}

internal class InternalFireParams : FireParams, InternalBillboardVertexParams() {

    override var strength = 3.0f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["strength"] = strength
        super.collect(mb)
    }
}

internal class InternalSmokeParams : SmokeParams, InternalBillboardVertexParams() {

    override var density = 0.5f
    override var seed = 0f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["density"] = density
        mb.uniforms["seed"] = seed
        super.collect(mb)
    }
}

internal class InternalWaterParams : WaterParams, InternalBaseParams() {

    override var waterColor: ColorRGB = ColorRGB(0.1f, 0.2f, 0.3f)
    override var transparency: Float = 0.1f
    override var waveScale: Float = 0.04f

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["waterColor"] = waterColor
        mb.uniforms["transparency"] = transparency
        mb.uniforms["waveScale"] = waveScale
        super.collect(mb)
    }
}

internal class InternalFastCloudSkyParams : FastCloudSkyParams, InternalBaseParams() {

    override var density = 3.0f     // 0..5
    override var thickness = 10.0f  // 0..20
    override var scale = 1.0f       // 0.1..10
    override var rippleamount = 0.3f  // 0..1
    override var ripplescale = 4.0f  // 1..10
    override var darkblue = ColorRGB(0.2f, 0.4f, 0.6f)
    override var lightblue = ColorRGB(0.4f, 0.6f, 1.0f)

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["density"] = density
        mb.uniforms["thickness"] = thickness
        mb.uniforms["scale"] = scale
        mb.uniforms["darkblue"] = darkblue
        mb.uniforms["lightblue"] = lightblue
        mb.uniforms["rippleamount"] = rippleamount
        mb.uniforms["ripplescale"] = ripplescale
        super.collect(mb)
    }
}

internal class InternalStarrySkyParams : StarrySkyParams, InternalBaseParams() {

    override var colorness = 0.8f;
    override var density = 20.0f;
    override var speed = 1.0f;
    override var size = 15.0f;

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["colorness"] = colorness
        mb.uniforms["density"] = density
        mb.uniforms["speed"] = speed
        mb.uniforms["size"] = size
        super.collect(mb)
    }
}

internal class InternalStandartParams : StandartParams, InternalBaseParams() {

    private var _specularGlossiness: SpecularGlossiness? = null

    override var baseColor = ColorRGBA.white(1.0f)
    override var emissiveFactor = ColorRGB.Black
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

    override fun collect(mb: MaterialBuilder) {

        mb.uniforms["baseColor"] = baseColor
        mb.uniforms["baseColorTexture"] = baseColorTexture

        mb.uniforms["metallic"] = pbr.metallic
        mb.uniforms["roughness"] = pbr.roughness
        mb.uniforms["metallicRoughnessTexture"] = pbr.metallicRoughnessTexture

        mb.uniforms["emissiveFactor"] = emissiveFactor
        mb.uniforms["emissiveTexture"] = emissiveTexture

//            mb.uniforms["occlusionTexture"] = _pbr!!.occlusionTexture

        pbr.metallicRoughnessTexture?.let { mb.shaderDefs += "METALLIC_ROUGHNESS_MAP" }
//          _pbr!!.occlusionTexture?.let { defs += "OCCLUSION_MAP" }

        if (_specularGlossiness != null) {
            mb.uniforms["specularFactor"] = _specularGlossiness!!.specularFactor
            mb.uniforms["glossinessFactor"] = _specularGlossiness!!.glossinessFactor
            mb.uniforms["specularGlossinessTexture"] = _specularGlossiness!!.specularGlossinessTexture
            mb.shaderDefs += "SPECULAR_GLOSSINESS"
            _specularGlossiness!!.specularGlossinessTexture?.let { mb.shaderDefs += "SPECULAR_GLOSSINESS_MAP" }
        }

        mb.uniforms["normalTexture"] = normalTexture
        mb.uniforms["shadowTexture"] = shadowTexture
        mb.uniforms["triplanarScale"] = triplanarScale

        mb.uniforms["jntMatrices[0]"] = jntMatrices

        mb.uniforms["xscale"] = xscale
        mb.uniforms["yscale"] = yscale
        mb.uniforms["rotation"] = rotation

        baseColorTexture?.let { mb.shaderDefs += "BASE_COLOR_MAP" }
        normalTexture?.let { mb.shaderDefs += "NORMAL_MAP" }
        emissiveTexture?.let { mb.shaderDefs += "EMISSIVE_MAP" }

        jntMatrices?.let { mb.shaderDefs += "SKINNING" }
        triplanarScale?.let { mb.shaderDefs += "TRIPLANAR" }

        super.collect(mb)
    }

    internal class InternalPbr : Pbr {
        override var metallic = 0.1f
        override var roughness = 0.5f
        override var metallicRoughnessTexture: TextureDeclaration? = null

//        override var occlusionTexture: TextureDeclaration? = null

    }

    internal class InternalSpecularGlossiness : SpecularGlossiness {
        override var specularFactor: ColorRGB = ColorRGB.White
        override var glossinessFactor: Float = 0.2f
        override var specularGlossinessTexture: TextureDeclaration? = null
    }
}

internal class InternalFogParams : FogParams, InternalBaseParams() {

    override var density = 0.02f
    override var color = ColorRGB.white(0.01f)

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["density"] = density
        mb.uniforms["fogColor"] = color
    }
}

internal class InternalSsrParams : SsrParams, InternalBaseParams() {

    override var samples = 16
    override var envTexture: CubeTextureDeclaration? = null

    override fun collect(mb: MaterialBuilder) {
        mb.uniforms["samples"] = samples
        envTexture?.let {
            mb.uniforms["envTexture"] = it
            mb.shaderDefs += "SSR_ENV"
        }
        super.collect(mb)
    }
}

internal class InternalBloomParams : BloomParams, InternalBaseParams() {
    override fun collect(mb: MaterialBuilder) {
        super.collect(mb)
    }
}

internal class InternalTerrainParams : TerrainParams, InternalBaseParams() {

    override var heightTexture: TextureDeclaration? = null
    override var heightTextureSize: Int? = null
    override var heightScale: Float = 10.0f
    override var terrainCenter: Vec3 = Vec3.ZERO
    override var outsideHeight: Float = 0f

    override fun collect(mb: MaterialBuilder) {
        mb.shaderDefs += "TERRAIN"
        mb.uniforms["heightTexture"] = heightTexture
        mb.uniforms["heightTextureSize"] = heightTextureSize
        mb.uniforms["heightScale"] = heightScale
        mb.uniforms["terrainCenter"] = terrainCenter
        mb.uniforms["outsideHeight"] = outsideHeight
        super.collect(mb)
    }
}

internal class InternalPostShadingEffect(
    val name: String,
    val width: Int,
    val height: Int,
    val effectPassMaterialModifiers: List<InternalMaterialModifier>,
    val compositionColorOutput: String,
    val compositionMaterialModifier: InternalMaterialModifier,
) : PostShadingEffect
