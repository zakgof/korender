package com.zakgof.korender

import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

interface Material

interface BillboardMaterial : Material

interface TerrainMaterial : Material

interface PipeMaterial : Material

interface PostProcessingMaterial : Material

interface SkyMaterial: Material

data class SpecularGlossiness(
    val specularFactor: ColorRGB,
    val glossinessFactor: Float,
)

interface BillboardEffect

interface MaterialContext {
    /**
     * Creates a material modifier that applies shader definitions.
     *
     * Definitions affect the #ifdef / #ifndef directives in shaders
     *
     * @param defs fragment shader defines
     * @return material modifier
     */
    fun defs(vararg defs: String)

    /**
     * Creates a material modifier that applies a shader plugin.
     *
     * @param name plugin mount point name
     * @param shaderFile plugin shader code resource file
     * @return material modifier
     */
    fun plugin(name: String, shaderFile: String)

    fun float(key: String, value: Float)
    fun int(key: String, value: Int)
    fun vec3(key: String, value: Vec3)
    fun texture(key: String, value: TextureDeclaration)
}

interface BaseMaterialContext : MaterialContext {

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
     * Specular-glossiness PBR model factors.
     * Set to enable specular-glossiness PBR flavor for the base material.
     */
    var specularGlossiness: SpecularGlossiness?

    /**
     * Specular-glossiness texture (r channel for specular, g for glossiness).
     * Set to enable specular-glossiness texturing for the base material.
     */
    var specularGlossinessTexture: TextureDeclaration?

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

    var ibl: SkyMaterial?
}

interface BillboardMaterialContext : BaseMaterialContext {
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

interface TerrainMaterialContext: BaseMaterialContext {

    /**
     * Creates a material modifier for clipmap terrains.
     * Used with the base material.
     *
     * @param heightTexture texture declaration for the heightmap, must be square. Red channel is used for elevation value
     * @param heightScale height scale: world space terrain elevation value for max texture sample value
     * @param outsideHeight world space elevation valur for points outside the texture range
     * @param terrainCenter world space point corresponding to terrain center
     * @return material modifier
     */

    var heightTexture: TextureDeclaration?
    var heightScale: Float
    var outsideHeight: Float
    var terrainCenter: Vec3
}

interface PostProcessMaterialContext: MaterialContext {

}


interface PostShadingEffect

interface PostProcessingEffect
