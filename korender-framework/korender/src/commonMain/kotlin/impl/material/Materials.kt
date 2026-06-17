package com.zakgof.korender.impl.material

import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.BillboardEffect
import com.zakgof.korender.BillboardMaterial
import com.zakgof.korender.BillboardMaterialScope
import com.zakgof.korender.DecalMaterial
import com.zakgof.korender.Material
import com.zakgof.korender.MaterialScope
import com.zakgof.korender.PipeMaterial
import com.zakgof.korender.PostProcessMaterialScope
import com.zakgof.korender.PostProcessingMaterial
import com.zakgof.korender.ShaderFlag
import com.zakgof.korender.ShaderPlugin
import com.zakgof.korender.ShadingMaterialScope
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.SpecularGlossinessScope
import com.zakgof.korender.TerrainMaterialScope
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.glgpu.ColorRGBAGetter
import com.zakgof.korender.impl.glgpu.ColorRGBGetter
import com.zakgof.korender.impl.glgpu.CompositeSupplier
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.IntGetter
import com.zakgof.korender.impl.glgpu.Mat4Getter
import com.zakgof.korender.impl.glgpu.Mat4ListGetter
import com.zakgof.korender.impl.glgpu.TextureGetter
import com.zakgof.korender.impl.glgpu.UniformGetter
import com.zakgof.korender.impl.glgpu.UniformPack
import com.zakgof.korender.impl.glgpu.UniformSupplier
import com.zakgof.korender.impl.glgpu.Vec2Getter
import com.zakgof.korender.impl.glgpu.Vec3Getter
import com.zakgof.korender.impl.model.InternalModelInfo
import com.zakgof.korender.impl.model.terrain.TerrainMaterialModifier
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class InternalSpecularGlossinessScope : SpecularGlossinessScope {
    override var specularFactor: ColorRGB = ColorRGB(1f, 1f, 1f)
    override var glossinessFactor: Float = 1f
    override var texture: TextureDeclaration? = null
}

internal open class InternalMaterialModifier(vararg getters: Pair<String, UniformGetter<*>>) :
    MaterialScope, UniformSupplier {

    var customDefs = 0L
    private var customPlugins1 = 0L
    private var customPlugins2 = 0L
    private val customFloatUniforms = mutableMapOf<String, Float>()
    private val customIntUniforms = mutableMapOf<String, Int>()
    private val customVec2Uniforms = mutableMapOf<String, Vec2>()
    private val customVec3Uniforms = mutableMapOf<String, Vec3>()
    private val gettersMap = getters.toMap()
    val customTextureUniforms = mutableMapOf<String, Any>()

    open fun collectDefs(accumulator: Long): Long = accumulator or customDefs

    open fun collectPlugins1(accumulator: Long): Long = accumulator.pluginOverride(customPlugins1)
    open fun collectPlugins2(accumulator: Long): Long = accumulator.pluginOverride(customPlugins2)

    override fun flags(vararg flags: ShaderFlag) {
        customDefs = flags.fold(0L) { acc, f -> acc.or((f as Defs).bit) }
    }

    override fun plugin(plugin: ShaderPlugin) {
        plugin as AppliedPlugin
        customPlugins1 = plugin.apply1(customPlugins1)
        customPlugins2 = plugin.apply2(customPlugins2)
    }

    override fun float(key: String, value: Float) {
        customFloatUniforms[key] = value
    }

    override fun int(key: String, value: Int) {
        customIntUniforms[key] = value
    }

    override fun vec2(key: String, value: Vec2) {
        customVec2Uniforms[key] = value
    }

    override fun vec3(key: String, value: Vec3) {
        customVec3Uniforms[key] = value
    }

    override fun texture(key: String, value: TextureDeclaration) {
        customTextureUniforms[key] = value
    }

    override fun uniform(name: String): UniformGetter<*>? =
        customFloatUniforms[name]?.let { FloatGetter<InternalMaterialModifier> { it.customFloatUniforms[name] } } ?: customIntUniforms[name]?.let { IntGetter<InternalMaterialModifier> { it.customIntUniforms[name] } }
        ?: customVec2Uniforms[name]?.let { Vec2Getter<InternalMaterialModifier> { it.customVec2Uniforms[name] } }
        ?: customVec3Uniforms[name]?.let { Vec3Getter<InternalMaterialModifier> { it.customVec3Uniforms[name] } } ?: customTextureUniforms[name]?.let { TextureGetter<InternalMaterialModifier> { it.customTextureUniforms[name] } }
        ?: gettersMap[name]
}

internal open class InternalMaterial(
    vertexShaderFile: String,
    deferredFragmentShaderFile: String,
    forwardFragmentShaderFile: String,
    vararg getters: Pair<String, UniformGetter<*>>,
) : InternalMaterialModifier(*getters), MaterialScope, Material {

    constructor(vertexShaderFile: String, fragmentShaderFile: String, vararg getters: Pair<String, UniformGetter<*>>) :
            this(vertexShaderFile, fragmentShaderFile, fragmentShaderFile, *getters)

    open val vertexShaderFile: String = vertexShaderFile
    open val deferredFragmentShaderFile: String = deferredFragmentShaderFile
    open val forwardFragmentShaderFile: String = forwardFragmentShaderFile

    var time = 0f

    override fun uniform(name: String) = if (name == "time") FloatGetter<InternalMaterial> { it.time } else super.uniform(name)

    fun toDeclaration(
        deferredShading: Boolean,
        nodeContext: NodeContext,
        uniformPack: UniformPack,
    ): ShaderDeclaration {
        var defs = 0L
        var plugins1 = 0L
        var plugins2 = 0L
        uniformPack[uniformPack.size - 2] = this as UniformSupplier
        (this as? CompositeSupplier)?.let { uniformPack[uniformPack.size - 1] = it.child as UniformSupplier? }
        uniformPack.forEach {
            (it as? InternalMaterialModifier)?.let { modifier ->
                defs = modifier.collectDefs(defs)
                plugins1 = modifier.collectPlugins1(plugins1)
                plugins2 = modifier.collectPlugins2(plugins2)
            }
        }
        return ShaderDeclaration(
            vertexShaderFile,
            if (deferredShading) deferredFragmentShaderFile else forwardFragmentShaderFile,
            defs,
            plugins1,
            plugins2,
            uniformPack,
            nodeContext
        )
    }
}

internal open class InternalBaseMaterial(vertexShaderFile: String = "!shader/base.vert") :
    InternalMaterial(vertexShaderFile, "!shader/deferred/geometry.frag", "!shader/forward.frag"),
    BaseMaterialScope, UniformSupplier, CompositeSupplier {

    override var color: ColorRGBA = ColorRGBA.White
    override var colorTexture: TextureDeclaration? = null
    override var metallicFactor: Float = 0.1f
    override var roughnessFactor: Float = 0.5f
    override var alphaCutoff: Float = 0.01f

    override var triplanarScale: Float? = null
    override var stochasticSharpness: Float? = null
    override var colorTextures: TextureArrayDeclaration? = null
    override var normalTexture: TextureDeclaration? = null
    override var emission: ColorRGB? = null
    override var metallicRoughnessTexture: TextureDeclaration? = null
    private var specularGlossinessScope: InternalSpecularGlossinessScope? = null

    override fun specularGlossiness(block: SpecularGlossinessScope.() -> Unit) {
        val scope = specularGlossinessScope ?: InternalSpecularGlossinessScope().also { specularGlossinessScope = it }
        scope.block()
    }
    override var emissionTexture: TextureDeclaration? = null
    override var occlusionTexture: TextureDeclaration? = null
    override var env: SkyMaterial? = null
    var jntMatrices: List<Mat4>? = null
    var model: Mat4 = Mat4.IDENTITY

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "baseColor" -> ColorRGBAGetter<InternalBaseMaterial> { it.color }
            "albedoTexture" -> TextureGetter<InternalBaseMaterial> { it.colorTexture }
            "metallicFactor" -> FloatGetter<InternalBaseMaterial> { it.metallicFactor }
            "roughnessFactor" -> FloatGetter<InternalBaseMaterial> { it.roughnessFactor }
            "alphaCutoff" -> FloatGetter<InternalBaseMaterial> { it.alphaCutoff }
            "colorTextures" -> TextureGetter<InternalBaseMaterial> { it.colorTextures }
            "triplanarScale" -> FloatGetter<InternalBaseMaterial> { it.triplanarScale }
            "stochasticSharpness" -> FloatGetter<InternalBaseMaterial> { it.stochasticSharpness }
            "normalTexture" -> TextureGetter<InternalBaseMaterial> { it.normalTexture }
            "emissionFactor" -> ColorRGBGetter<InternalBaseMaterial> { it.emission!! }
            "metallicRoughnessTexture" -> TextureGetter<InternalBaseMaterial> { it.metallicRoughnessTexture }
            "specularFactor" -> ColorRGBGetter<InternalBaseMaterial> { it.specularGlossinessScope?.specularFactor }
            "glossinessFactor" -> FloatGetter<InternalBaseMaterial> { it.specularGlossinessScope?.glossinessFactor }
            "specularGlossinessTexture" -> TextureGetter<InternalBaseMaterial> { it.specularGlossinessScope?.texture }
            "occlusionTexture" -> TextureGetter<InternalBaseMaterial> { it.occlusionTexture }
            "emissionTexture" -> TextureGetter<InternalBaseMaterial> { it.emissionTexture }
            "jntMatrices[0]" -> Mat4ListGetter<InternalBaseMaterial> { it.jntMatrices }
            "model" -> Mat4Getter<InternalBaseMaterial> { it.model }
            else -> super.uniform(name)
        }

    override fun collectPlugins1(accumulator: Long) = super.collectPlugins1(accumulator)
        .pluginOverride1IfNotNull(colorTexture, Plugins.TEXSOURCE_TEXTURE)
        .pluginOverride1IfNotNull(colorTextures, Plugins.TEXSOURCE_ARRAY)
        .pluginOverride1IfNotNull(triplanarScale, Plugins.TEXTURING_TRIPLANAR)
        .pluginOverride1IfNotNull(stochasticSharpness, Plugins.TEXTURING_STOCHASTIC)
        .pluginOverride1IfNotNull(normalTexture, Plugins.NORMAL_TEXTURE)
        .pluginOverride1IfNotNull(emission, Plugins.EMISSION_FACTOR)
        .pluginOverride1IfNotNull(metallicRoughnessTexture, Plugins.METALLIC_ROUGHNESS_TEXTURE)
        .pluginOverride1IfNotNull(specularGlossinessScope, Plugins.SPECULAR_GLOSSINESS_FACTOR)
        .pluginOverride1IfNotNull(specularGlossinessScope?.texture, Plugins.SPECULAR_GLOSSINESS_TEXTURE)
        .pluginOverride1IfNotNull(occlusionTexture, Plugins.OCCLUSION_TEXTURE)
        .pluginOverride1IfNotNull(emissionTexture, Plugins.EMISSION_TEXTURE)

    override fun collectDefs(accumulator: Long) = super.collectDefs(accumulator)
        .combineDefsIfNotNull(triplanarScale, Defs.TRIPLANAR)

    override val child
        get() = env as? InternalMaterialModifier

    fun toMaterialInfo() = InternalModelInfo.Material(
        color = this.color,
        colorTextureResource = colorTexture,
        metallicFactor = metallicFactor,
        roughnessFactor = roughnessFactor,
        alphaCutoff = alphaCutoff,
        triplanarScale = triplanarScale,
        stochasticSharpness = stochasticSharpness,
        normalTextureResource = normalTexture,
        emission = emission,
        metallicRoughnessTextureResource = metallicRoughnessTexture,
        emissionTextureResource = emissionTexture,
        occlusionTextureResource = occlusionTexture
    )
}

internal class InternalBillboardMaterial : InternalBaseMaterial("!shader/billboard.vert"),
    BillboardMaterial, BillboardMaterialScope {

    override var position: Vec3 = Vec3.ZERO
    override var scale: Vec2 = Vec2(1f, 1f)
    override var rotation: Float = 0f
    override var effect: BillboardEffect? = null

    override val deferredFragmentShaderFile
        get() = (effect as? InternalBillboardEffect)?.fragmentShaderFile ?: super.deferredFragmentShaderFile
    override val forwardFragmentShaderFile
        get() = (effect as? InternalBillboardEffect)?.fragmentShaderFile ?: super.forwardFragmentShaderFile
    override val child
        get() = effect as? InternalMaterialModifier

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "pos" -> Vec3Getter<InternalBillboardMaterial> { it.position }
            "scale" -> Vec2Getter<InternalBillboardMaterial> { it.scale }
            "rotation" -> FloatGetter<InternalBillboardMaterial> { it.rotation }
            else -> super.uniform(name)
        }
}

internal class InternalDecalMaterial : InternalBaseMaterial(), DecalMaterial {
    override val vertexShaderFile = "!shader/deferred/decal.vert"
    override val deferredFragmentShaderFile = "!shader/deferred/decal.frag"
}

internal data class InternalTerrainMaterial(val modifier: TerrainMaterialModifier) : InternalBaseMaterial("!shader/terrain.vert"), TerrainMaterialScope {

    class HeightTexturePlugin(
        val heightTexture: TextureDeclaration?,
        val heightScale: Float,
        val outsideHeight: Float,
        val terrainCenter: Vec3,
    )

    var heightTexturePlugin: HeightTexturePlugin? = null

    override fun heightTexture(heightTexture: TextureDeclaration, heightScale: Float, outsideHeight: Float, terrainCenter: Vec3) {
        heightTexturePlugin = HeightTexturePlugin(heightTexture, heightScale, outsideHeight, terrainCenter)
    }

    // TODO ugly
    override fun collectPlugins1(accumulator: Long): Long = super.collectPlugins1(
        0L
            .pluginOverride1(Plugins.NORMAL_TERRAIN)
            .pluginOverride1IfNotNull(heightTexturePlugin, Plugins.TERRAIN_TEXTURE)
    )

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "heightTexture" -> TextureGetter<InternalTerrainMaterial> { it.heightTexturePlugin?.heightTexture }
            "heightScale" -> FloatGetter<InternalTerrainMaterial> { it.heightTexturePlugin?.heightScale }
            "outsideHeight" -> FloatGetter<InternalTerrainMaterial> { it.heightTexturePlugin?.outsideHeight }
            "terrainCenter" -> Vec3Getter<InternalTerrainMaterial> { it.heightTexturePlugin?.terrainCenter }
            else -> super.uniform(name)
        }

    override val child
        get() = modifier
}

internal class InternalPipeMaterial : InternalBaseMaterial("!shader/pipe.vert"), PipeMaterial {

    override fun collectPlugins1(accumulator: Long): Long = super.collectPlugins1(accumulator)
        .pluginOverride1(Plugins.POSITION_PIPE)
        .pluginOverride1(Plugins.NORMAL_PIPE)
        .pluginOverride1(Plugins.DEPTH_PIPE)
}

internal open class InternalPostProcessingMaterial(
    fragmentShaderFile: String,
    vararg getters: Pair<String, UniformGetter<*>>,
) : InternalMaterial("!shader/screen.vert", fragmentShaderFile, *getters), PostProcessingMaterial, PostProcessMaterialScope

internal abstract class InternalBillboardEffect(val fragmentShaderFile: String, vararg getters: Pair<String, UniformGetter<*>>) : BillboardEffect, InternalMaterialModifier(*getters)

internal open class InternalSkyMaterial(val skyPlugin: String, vararg getters: Pair<String, UniformGetter<*>>) :
    InternalMaterial("!shader/sky/sky.vert", "!shader/sky/sky.frag", *getters),
    SkyMaterial {

    override fun collectPlugins1(accumulator: Long): Long {
        var result = super.collectPlugins1(accumulator)
        val plugin = when (skyPlugin) {
            "!shader/plugin/sky.fastcloud.frag" -> Plugins.SKY_FASTCLOUD
            "!shader/plugin/sky.starry.frag" -> Plugins.SKY_STARRY
            "!shader/plugin/sky.cube.frag" -> Plugins.SKY_CUBE
            "!shader/plugin/sky.texture.frag" -> Plugins.SKY_TEXTURE
            else -> null
        }
        return result.pluginOverride1IfNotNull(plugin, plugin!!)
    }
}

internal class DecalBlendMaterial(
    val decalAlbedo: GlGpuTexture,
    val decalNormal: GlGpuTexture,
) : InternalMaterial(
    "!shader/screen.vert", "!shader/deferred/decalblend.frag",
) {
    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "decalAlbedo" -> TextureGetter<DecalBlendMaterial> { it.decalAlbedo }
            "decalNormal" -> TextureGetter<DecalBlendMaterial> { it.decalNormal }
            else -> super.uniform(name)
        }
}

internal class InstancingMaterialModifier(val defs: Long) : InternalMaterialModifier(
    "jntTexture" to TextureGetter<InstancingMaterialModifier> { it.jntTexture }
) {
    var jntTexture: GlGpuTexture? = null

    override fun collectDefs(accumulator: Long): Long =
        super.collectDefs(accumulator) or defs
}

internal class InternalShadingMaterial() :
    InternalMaterial("!shader/screen.vert", "!shader/deferred/shading.frag"),
    CompositeSupplier, ShadingMaterialScope {

    override var env: SkyMaterial? = null

    override val child
        get() = env as? InternalMaterialModifier
}
