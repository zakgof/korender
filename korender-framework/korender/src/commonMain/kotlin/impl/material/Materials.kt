package com.zakgof.korender.impl.material

import com.zakgof.korender.BaseMaterialContext
import com.zakgof.korender.BillboardEffect
import com.zakgof.korender.BillboardMaterial
import com.zakgof.korender.BillboardMaterialContext
import com.zakgof.korender.Material
import com.zakgof.korender.MaterialContext
import com.zakgof.korender.PipeMaterial
import com.zakgof.korender.PostProcessingMaterial
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.SpecularGlossiness
import com.zakgof.korender.TerrainMaterial
import com.zakgof.korender.TerrainMaterialContext
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.impl.engine.MaterialDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.glgpu.ColorRGBAGetter
import com.zakgof.korender.impl.glgpu.ColorRGBGetter
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.TextureGetter
import com.zakgof.korender.impl.glgpu.UniformGetter
import com.zakgof.korender.impl.glgpu.UniformSupplier
import com.zakgof.korender.impl.glgpu.Vec2Getter
import com.zakgof.korender.impl.glgpu.Vec3Getter
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

//internal class MaterialBuilder(base: BaseMaterial, deferredShading: Boolean, private val retentionPolicy: RetentionPolicy) {
//
//    var vertShaderFile: String = when (base) {
//        BaseMaterial.Renderable -> "!shader/base.vert"
//        BaseMaterial.Billboard -> "!shader/billboard.vert"
//        BaseMaterial.Screen, BaseMaterial.Shading, BaseMaterial.Composition, BaseMaterial.DecalBlend -> "!shader/screen.vert"
//        BaseMaterial.Font -> "!shader/gui/font.vert"
//        BaseMaterial.Image -> "!shader/gui/image.vert"
//        BaseMaterial.Sky -> "!shader/sky/sky.vert"
//        BaseMaterial.Decal -> "!shader/deferred/decal.vert"
//    }
//    var fragShaderFile: String = when (base) {
//        BaseMaterial.Renderable, BaseMaterial.Billboard -> if (deferredShading) "!shader/deferred/geometry.frag" else "!shader/forward.frag"
//        BaseMaterial.Screen -> "!shader/screen.frag"
//        BaseMaterial.Font -> "!shader/gui/font.frag"
//        BaseMaterial.Image -> "!shader/gui/image.frag"
//        BaseMaterial.Sky -> ""
//        BaseMaterial.Shading ->
//        BaseMaterial.Composition -> "!shader/deferred/composition.frag"
//        BaseMaterial.Decal -> "!shader/deferred/decal.frag"
//        BaseMaterial.DecalBlend -> "!shader/deferred/decalblend.frag"
//    }


internal class InternalShader(
    val vertexShaderFile: String,
    val deferredFragmentShaderFile: String,
    val forwardFragmentShaderFile: String,
) {
    constructor(vertexShaderFile: String, fragmentShaderFile: String)
            : this(vertexShaderFile, fragmentShaderFile, fragmentShaderFile)
}

internal open class InternalMaterialModifier : MaterialContext, UniformSupplier {

    private val customDefs = mutableSetOf<String>()
    private val customPlugins = mutableListOf<Pair<String, String>>()
    private val customUniforms = mutableMapOf<String, Any?>()

    open val defs: Set<String>
        get() = setOf()

    open val plugins: List<Pair<String, String>>
        get() = listOf()

    val totalDefs: Set<String>
        get() = defs + customDefs

    val totalPlugins: List<Pair<String, String>>
        get() = plugins + customPlugins

    override fun defs(vararg defs: String) {
        this.customDefs += defs
    }

    override fun plugin(name: String, shaderFile: String) {
        this.customPlugins += name to shaderFile
    }

    override fun uniforms(vararg pairs: Pair<String, Any?>) {
        customUniforms += pairs
    }

    override fun uniform(name: String): UniformGetter<*>? {
        return when (customUniforms[name]) {
            is Float -> FloatGetter<InternalMaterialModifier> { customUniforms[name] as Float }
            is Vec3 -> Vec3Getter<InternalMaterialModifier> { customUniforms[name] as Vec3 }
            else -> null
        }
    }
}

internal open class InternalMaterial(
    vertexShaderFile: String,
    deferredFragmentShaderFile: String,
    forwardFragmentShaderFile: String,
) : InternalMaterialModifier(), MaterialContext, Material {

    constructor(vertexShaderFile: String, fragmentShaderFile: String) :
            this(vertexShaderFile, fragmentShaderFile, fragmentShaderFile)

    open val internalShader = InternalShader(vertexShaderFile, deferredFragmentShaderFile, forwardFragmentShaderFile)

    fun toDeclaration(
        deferredShading: Boolean,
        retentionPolicy: RetentionPolicy,
        modifiers: List<InternalMaterialModifier>,
    ) =
        MaterialDeclaration(
            ShaderDeclaration(
                internalShader.vertexShaderFile,
                if (deferredShading) internalShader.deferredFragmentShaderFile else internalShader.forwardFragmentShaderFile,
                totalDefs + modifiers.flatMap { it.totalDefs },
                (totalPlugins + modifiers.flatMap { it.totalPlugins }).toMap(),
                retentionPolicy
            ),
            listOf(this) + modifiers
        )
}

internal open class InternalBaseMaterial(vertexShaderFile: String = "!shader/base.vert") :
    InternalMaterial(vertexShaderFile, "!shader/deferred/geometry.frag", "!shader/forward.frag"),
    BaseMaterialContext, UniformSupplier {

    override var color: ColorRGBA = ColorRGBA.White
    override var colorTexture: TextureDeclaration? = null
    override var metallicFactor: Float = 0.1f
    override var roughnessFactor: Float = 0.5f
    override var alphaCutoff: Float = 0.01f

    override var triplanarScale: Float? = null
    override var colorTextures: TextureArrayDeclaration? = null
    override var normalTexture: TextureDeclaration? = null
    override var emission: ColorRGB? = null
    override var metallicRoughnessTexture: TextureDeclaration? = null
    override var specularGlossiness: SpecularGlossiness? = null
    override var specularGlossinessTexture: TextureDeclaration? = null
    override var emissionTexture: TextureDeclaration? = null
    override var occlusionTexture: TextureDeclaration? = null
    override var ibl: SkyMaterial? = null

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "baseColor" -> ColorRGBAGetter(InternalBaseMaterial::color)
            "baseColorTexture" -> TextureGetter(InternalBaseMaterial::colorTexture)
            "metallicFactor" -> FloatGetter(InternalBaseMaterial::metallicFactor)
            "roughnessFactor" -> FloatGetter(InternalBaseMaterial::roughnessFactor)
            "alphaCutoff" -> FloatGetter(InternalBaseMaterial::alphaCutoff)
            "colorTextures" -> TextureGetter(InternalBaseMaterial::colorTextures)
            "triplanarScale" -> TextureGetter(InternalBaseMaterial::triplanarScale)
            "normalTexture" -> TextureGetter(InternalBaseMaterial::normalTexture)
            "emissionFactor" -> ColorRGBGetter<InternalBaseMaterial> { it.emission!! }
            "metallicRoughnessTexture" -> TextureGetter(InternalBaseMaterial::metallicRoughnessTexture)
            "specularFactor" -> ColorRGBGetter<InternalBaseMaterial> { it.specularGlossiness?.specularFactor }
            "glossinessFactor" -> FloatGetter<InternalBaseMaterial> { it.specularGlossiness?.glossinessFactor }
            "specularGlossinessTexture" -> TextureGetter(InternalBaseMaterial::specularGlossinessTexture)
            "occlusionTexture" -> TextureGetter(InternalBaseMaterial::occlusionTexture)
            "emissionTexture" -> TextureGetter(InternalBaseMaterial::emissionTexture)
            else -> super.uniform(name)
        }

    override val defs
        get() = setOfNotNull(
            colorTexture?.let { "BASE_COLOR_MAP" },
            colorTextures?.let { "TEXTURE_ARRAY" }
        )

    override val plugins
        get() = listOfNotNull(
            colorTextures?.let { "texturing" to "!shader/plugin/texturing.array.frag" },
            triplanarScale?.let { "texturing" to "!shader/plugin/texturing.triplanar.frag" },
            normalTexture?.let { "normal" to "!shader/plugin/normal.texture.frag" },
            emission?.let { "emission" to "!shader/plugin/normal.texture.frag" },
            metallicRoughnessTexture?.let { "metallic_roughness" to "!shader/plugin/metallic_roughness.texture.frag" },
            specularGlossiness?.let { "specular_glossiness" to "!shader/plugin/specular_glossiness.factor.frag" },
            specularGlossinessTexture?.let { "specular_glossiness" to "!shader/plugin/specular_glossiness.texture.frag" },
            occlusionTexture?.let { "occlusion" to "!shader/plugin/occlusion.texture.frag" },
            emissionTexture?.let { "emission" to "!shader/plugin/emission.texture.frag" }
        )
}

internal class InternalBillboardMaterial : InternalBaseMaterial(), BillboardMaterial, BillboardMaterialContext {

    override var position: Vec3 = Vec3.ZERO
    override var scale: Vec2 = Vec2(1f, 1f)
    override var rotation: Float = 0f
    override var effect: BillboardEffect? = null

    override val internalShader
        get() = InternalShader(
            super.internalShader.vertexShaderFile,
            (effect as? InternalBillboardEffect)?.fragmentShaderFile ?: super.internalShader.deferredFragmentShaderFile,
            (effect as? InternalBillboardEffect)?.fragmentShaderFile ?: super.internalShader.forwardFragmentShaderFile
        )

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "position" -> Vec3Getter(InternalBillboardMaterial::position)
            "scale" -> Vec2Getter(InternalBillboardMaterial::scale)
            "rotation" -> FloatGetter(InternalBillboardMaterial::rotation)
            else -> super.uniform(name)
        }
}

internal class InternalTerrainMaterial : InternalBaseMaterial("!shader/terrain.vert"), TerrainMaterial, TerrainMaterialContext {

    override var heightTexture: TextureDeclaration? = null
    override var heightScale: Float = 0.1f
    override var outsideHeight: Float = 0f
    override var terrainCenter: Vec3 = Vec3.ZERO


    override val defs: Set<String>
        get() = setOf("TERRAIN")

    override val plugins: List<Pair<String, String>>
        get() = listOf(
            "normal" to "!shader/plugin/normal.terrain.frag",
            "terrain" to "!shader/plugin/terrain.texture.frag"
        )

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "heightTexture" -> TextureGetter(InternalTerrainMaterial::heightTexture)
            "heightScale" -> FloatGetter(InternalTerrainMaterial::heightScale)
            "outsideHeight" -> FloatGetter(InternalTerrainMaterial::outsideHeight)
            "terrainCenter" -> Vec3Getter(InternalTerrainMaterial::terrainCenter)
            else -> super.uniform(name)
        }
}

internal class InternalPipeMaterial : InternalBaseMaterial("!shader/pipe.vert"), PipeMaterial {

    override val plugins: List<Pair<String, String>>
        get() = listOf(
            "position" to "!shader/plugin/position.pipe.frag",
            "normal" to "!shader/plugin/normal.pipe.frag",
            "depth" to "!shader/plugin/depth.pipe.frag"
        )
}

internal class InternalPostProcessingMaterial(
    val fragmentShaderFile: String,
    vararg val uniforms: Pair<String, Any?>,
    val additionalCompile: () -> Unit = {},
) : InternalMaterial("!shader/screen.vert"), PostProcessingMaterial {

    override fun fragmentShaderFile(deferredShading: Boolean) = fragmentShaderFile

    override fun compile() {
        uniforms(*uniforms)
        additionalCompile()
    }
}

internal class InternalBillboardEffect(
    val fragmentShaderFile: String,
    vararg val uniforms: Pair<String, Any?>, // TODO
) : BillboardEffect

internal class InternalSkyMaterial(
    val skyPlugin: String,
    vararg val uniforms: Pair<String, Any?>,
) : InternalCustomMaterial("!shader/sky/sky.vert", "!shader/sky/sky.frag"), SkyMaterial {
    override fun compile() {
        plugin("sky", skyPlugin)
        uniforms(*uniforms)
    }
}

internal class ConstMaterialModifier(vararg getters: Pair<String, UniformGetter<ConstMaterialModifier>>) : InternalMaterialModifier() {
    private val map = getters.toMap()
    override fun uniform(name: String): UniformGetter<ConstMaterialModifier>? = map[name]
}