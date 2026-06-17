package com.zakgof.korender

import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

/**
 * Base material interface for all material types.
 * Materials define how objects are rendered, including lighting, texturing, and shader effects.
 */
interface Material

/**
 * Material for camera-facing quadrilateral billboards (sprites).
 * Billboards automatically rotate to face the camera, useful for particles, trees, etc.
 */
interface BillboardMaterial : Material

/**
 * Material for decal rendering on surfaces.
 * Decals are projected onto existing geometry without affecting the underlying mesh.
 */
interface DecalMaterial : Material

/**
 * Material for pipe meshes with custom rendering.
 * Typically used for deferred rendering pipelines.
 */
interface PipeMaterial : Material

/**
 * Material for post-processing effects applied to the final frame.
 * Post-processing shaders operate on the rendered frame buffer.
 */
interface PostProcessingMaterial : Material

/**
 * Material for sky rendering (background environment).
 * Can be procedural or texture-based (skybox, skydome).
 */
interface SkyMaterial : Material

/**
 * DSL scope for configuring the specular-glossiness PBR model.
 * Used as an alternative to metallic-roughness PBR model.
 */
interface SpecularGlossinessScope {

    /**
     * RGB color representing specular reflection.
     */
    var specularFactor: ColorRGB

    /**
     * Glossiness factor (0.0 = rough, 1.0 = mirror-like).
     */
    var glossinessFactor: Float

    /**
     * Specular-glossiness texture (rgb for specular, a for glossiness).
     * Set to enable specular-glossiness texturing.
     */
    var texture: TextureDeclaration?
}

/**
 * Effect applied to billboard rendering (e.g., particle effects).
 */
interface BillboardEffect

/**
 * Base interface for modifying material properties during rendering.
 * Provides access to set custom uniforms and shader plugins.
 */
interface MaterialScope {

    /**
     * Adds shader flags to this material.
     * @param flags custom shader plugin
     */
    fun flags(vararg flags: ShaderFlag)

    /**
     * Adds a custom shader plugin to this material.
     * @param plugin custom shader plugin
     */
    fun plugin(plugin: ShaderPlugin)

    /**
     * Sets a custom float uniform.
     * @param key uniform name
     * @param value uniform value
     */
    fun float(key: String, value: Float)

    /**
     * Sets a custom integer uniform.
     * @param key uniform name
     * @param value uniform value
     */
    fun int(key: String, value: Int)

    /**
     * Sets a custom 2D vector uniform.
     * @param key uniform name
     * @param value uniform value
     */
    fun vec2(key: String, value: Vec2)

    /**
     * Sets a custom 3D vector uniform.
     * @param key uniform name
     * @param value uniform value
     */
    fun vec3(key: String, value: Vec3)

    /**
     * Sets a custom texture uniform.
     * @param key uniform name
     * @param value texture declaration
     */
    fun texture(key: String, value: TextureDeclaration)
}

interface BaseMaterialScope : MaterialScope {

    /**
     * Base albedo color (multiplied by [colorTexture]).
     */
    var color: ColorRGBA

    /**
     * Base albedo texture (multiplied by [color]).
     */
    var colorTexture: TextureDeclaration?

    /**
     * Metallic factor in the PBR model.
     */
    var metallicFactor: Float

    /**
     * Roughness factor in the PBR model.
     */
    var roughnessFactor: Float

    /**
     * Alpha value threshold to discard fragments.
     */
    var alphaCutoff: Float

    /**
     * Triplanar mapping scale for mapping world coordinates into texture coordinates.
     * Set to enable triplanar rendering for the base material.
     */
    var triplanarScale: Float?

    /**
     * Sharpness factor for stochastic texture sampling.
     * Set to enable stochastic sampling to reduce texture repetitiveness patterns.
     */
    var stochasticSharpness: Float?

    /**
     * Texture array for texture-array-based texturing.
     * Set to enable texture array texturing for the base material.
     */
    var colorTextures: TextureArrayDeclaration?

    /**
     * Normal mapping texture.
     * Set to enable normal mapping for the base material.
     */
    var normalTexture: TextureDeclaration?

    /**
     * Emission factor (color).
     * Set to enable emission for the base material.
     */
    var emission: ColorRGB?

    /**
     * Metallic-roughness texture (r channel for metallic, g for roughness).
     * Set to enable metallic-roughness texturing for the base material.
     */
    var metallicRoughnessTexture: TextureDeclaration?

    /**
     * Configures the specular-glossiness PBR model.
     * Set to enable specular-glossiness PBR flavor for the base material.
     */
    fun specularGlossiness(block: SpecularGlossinessScope.() -> Unit)

    /**
     * Emission texture.
     * Set to enable emission texturing for the base material.
     */
    var emissionTexture: TextureDeclaration?

    /**
     * Pre-calculated occlusion texture.
     * Set to enable occlusion texturing for the base material.
     */
    var occlusionTexture: TextureDeclaration?

    fun detailTexture(block: DetailTextureScope.() -> Unit)

    var env: SkyMaterial?
}

/**
 * DSL scope for configuring detail texturing.
 * Blends a secondary detail texture over the base texture at a configurable scale and strength.
 */
interface DetailTextureScope {

    /**
     * Detail texture to overlay.
     */
    var texture: TextureDeclaration?

    /**
     * UV scale factor for the detail texture (default 1.0).
     * Higher values tile the detail texture more frequently across the surface.
     */
    var scale: Float

    /**
     * Blend strength of the detail texture (0.0 = no detail, 1.0 = full strength).
     */
    var strength: Float
}

interface BillboardMaterialScope : BaseMaterialScope {
    /**
     * Creates a material modifier for billboards.
     * Used with the base material.
     *
     * @param position billboard center position
     * @param scale billboard quad size
     * @param rotation rotation angle
     * @return material modifier
     */

    var position: Vec3
    var scale: Vec2
    var rotation: Float
    var effect: BillboardEffect?
}

interface TerrainMaterialScope : BaseMaterialScope {

    /**
     * TODO
     * Creates a material modifier for clipmap terrains.
     * Used with the base material.
     *
     * @param heightTexture texture declaration for the heightmap, must be square. Red channel is used for elevation value
     * @param heightScale height scale: world space terrain elevation value for max texture sample value
     * @param outsideHeight world space elevation valur for points outside the texture range
     * @param terrainCenter world space point corresponding to terrain center
     * @return material modifier
     */

    fun heightTexture(heightTexture: TextureDeclaration, heightScale: Float, outsideHeight: Float = 0f, terrainCenter: Vec3 = Vec3.ZERO)
}

interface PostProcessMaterialScope : MaterialScope


interface PostProcessingEffect


interface ShadingMaterialScope : MaterialScope {
    var env: SkyMaterial?
}
