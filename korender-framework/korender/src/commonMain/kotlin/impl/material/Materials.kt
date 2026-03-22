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


internal abstract class InternalMaterial(val vertexShaderFile: String) : MaterialContext, Material {

    private val defs = mutableSetOf<String>()
    private val plugins = mutableMapOf<String, String>()
    private val uniforms = mutableMapOf<String, Any?>()

    abstract fun fragmentShaderFile(deferredShading: Boolean): String

    override fun defs(vararg defs: String) {
        this.defs += defs
    }

    override fun plugin(name: String, shaderFile: String) {
        this.plugins += name to shaderFile
    }

    override fun uniforms(vararg pairs: Pair<String, Any?>) {
        uniforms += pairs
    }

    open fun compile() {
    }

    fun toDeclaration(
        deferredShading: Boolean,
        retentionPolicy: RetentionPolicy,
        contextPlugins: Map<String, String>,
        contextUniforms: Map<String, Any?>,
    ) =
        MaterialDeclaration(
            ShaderDeclaration(
                vertexShaderFile,
                fragmentShaderFile(deferredShading),
                defs,
                contextPlugins + plugins,
                retentionPolicy
            ),
            contextUniforms + uniforms
        )
}

// TODO support deferred shading
internal open class InternalCustomMaterial(
    vertexShaderFile: String,
    val fragmentShaderFile: String,
) : InternalMaterial(vertexShaderFile) {
    override fun fragmentShaderFile(deferredShading: Boolean) = fragmentShaderFile
}

internal open class InternalBaseMaterial(vertexShaderFile: String = "!shader/base.vert") : InternalMaterial(vertexShaderFile), BaseMaterialContext {

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

    override fun compile() {
        uniforms(
            "baseColor" to color,
            "baseColorTexture" to colorTexture,
            "metallicFactor" to metallicFactor,
            "roughnessFactor" to roughnessFactor,
            "alphaCutoff" to alphaCutoff
        )
        colorTexture?.let {
            defs("BASE_COLOR_MAP")
        }
        colorTextures?.let {
            defs("TEXTURE_ARRAY")
            plugin("texturing", "!shader/plugin/texturing.array.frag")
            uniforms("colorTextures" to it)
        }
        triplanarScale?.let {
            plugin("texturing", "!shader/plugin/texturing.triplanar.frag")
            uniforms("triplanarScale" to it)
        }

        normalTexture?.let {
            plugin("normal", "!shader/plugin/normal.texture.frag")
            uniforms("normalTexture" to it)
        }

        emission?.let {
            plugin("emission", "!shader/plugin/emission.factor.frag")
            uniforms("emissionFactor" to it)
        }

        metallicRoughnessTexture?.let {
            plugin("metallic_roughness", "!shader/plugin/metallic_roughness.texture.frag")
            uniforms("metallicRoughnessTexture" to it)
        }

        specularGlossiness?.let {
            plugin("specular_glossiness", "!shader/plugin/specular_glossiness.factor.frag")
            uniforms(
                "specularFactor" to it.specularFactor,
                "glossinessFactor" to it.glossinessFactor
            )
        }

        specularGlossinessTexture?.let {
            plugin("specular_glossiness", "!shader/plugin/specular_glossiness.texture.frag")
            uniforms("specularGlossinessTexture" to it)
        }

        occlusionTexture?.let {
            plugin("occlusion", "!shader/plugin/occlusion.texture.frag")
            uniforms("occlusionTexture" to it)
        }

        emissionTexture?.let {
            plugin("emission", "!shader/plugin/emission.texture.frag")
            uniforms("emissionTexture" to it)
        }


    }

    override fun fragmentShaderFile(deferredShading: Boolean) =
        if (deferredShading) "!shader/deferred/geometry.frag" else "!shader/forward.frag"
}

internal class InternalBillboardMaterial : InternalBaseMaterial(), BillboardMaterial, BillboardMaterialContext {
    override var position: Vec3 = Vec3.ZERO
    override var scale: Vec2 = Vec2(1f, 1f)
    override var rotation: Float = 0f
    override var effect: BillboardEffect? = null

    override fun fragmentShaderFile(deferredShading: Boolean) =
        (effect as? InternalBillboardEffect)?.fragmentShaderFile ?: super.fragmentShaderFile(deferredShading)

    override fun compile() {
        super.compile()
        uniforms(
            "pos" to position,
            "scale" to scale,
            "rotation" to rotation
        )
        (effect as? InternalBillboardEffect)?.let {
            uniforms(*it.uniforms)
        }
    }
}

internal class InternalTerrainMaterial : InternalBaseMaterial("!shader/terrain.vert"), TerrainMaterial, TerrainMaterialContext {

    override var heightTexture: TextureDeclaration? = null
    override var heightScale: Float = 0.1f
    override var outsideHeight: Float = 0f
    override var terrainCenter: Vec3 = Vec3.ZERO

    override fun compile() {
        super.compile()
        defs("TERRAIN")
        plugin("normal", "!shader/plugin/normal.terrain.frag")
        plugin("terrain", "!shader/plugin/terrain.texture.frag")
        uniforms(
            "heightTexture" to heightTexture,
            "heightScale" to heightScale,
            "outsideHeight" to outsideHeight,
            "terrainCenter" to terrainCenter
        )
    }
}

internal class InternalPipeMaterial : InternalBaseMaterial("!shader/pipe.vert"), PipeMaterial {

    override fun compile() {
        super.compile()
        plugin("position", "!shader/plugin/position.pipe.frag")
        plugin("normal", "!shader/plugin/normal.pipe.frag")
        plugin("depth", "!shader/plugin/depth.pipe.frag")
    }
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
    vararg val uniforms: Pair<String, Any?>,
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
