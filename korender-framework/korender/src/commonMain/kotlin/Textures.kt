package com.zakgof.korender

/**
 * Marker interface for a texture resource declaration.
 * Textures are typically loaded from image files during rendering.
 */
interface TextureDeclaration

/**
 * Texture declaration backed by a named resource path.
 * The resource is resolved by the active resource loader.
 */
interface ResourceTextureDeclaration : TextureDeclaration {
    val textureResource: String
}

/**
 * Texture declaration backed by image bytes loaded at runtime.
 * The bytes are expected to contain an image in the format identified by [extension].
 */
interface ByteArrayTextureDeclaration : TextureDeclaration {
    val fileBytesLoader: () -> ByteArray
    val extension: String
}

/**
 * Marker interface for a texture array declaration.
 * Texture arrays allow indexed access to multiple textures of the same size.
 */
interface TextureArrayDeclaration

/**
 * Marker interface for a 3D texture (volumetric texture) declaration.
 * 3D textures extend 2D textures with depth dimension.
 */
interface Texture3DDeclaration

/**
 * Marker interface for a cube texture (cubemap) declaration.
 * Cubemaps consist of 6 square textures forming a cube, useful for skyboxes and environment mapping.
 */
interface CubeTextureDeclaration

/**
 * Texture filtering mode.
 */
enum class TextureFilter {
    /** Nearest neighbor filtering (pixelated appearance, fastest) */
    Nearest,
    /** Linear filtering (smooth appearance, moderate performance) */
    Linear,
    /** Mipmap filtering (multiple resolution levels, best quality at distance) */
    MipMap
}

/**
 * Texture wrapping/tiling mode for coordinates outside [0.0, 1.0].
 */
enum class TextureWrap {
    /** Texture repeats with mirroring at edges (seamless tiling) */
    MirroredRepeat,
    /** Texture is clamped to edge color (no tiling) */
    ClampToEdge,
    /** Texture repeats without mirroring (may have visible seams) */
    Repeat
}

/**
 * Cubemap faces.
 */
enum class CubeTextureSide {
    /** Negative X face (-X direction) */
    NX,
    /** Negative Y face (-Y direction, bottom) */
    NY,
    /** Negative Z face (-Z direction) */
    NZ,
    /** Positive X face (+X direction) */
    PX,
    /** Positive Y face (+Y direction, top) */
    PY,
    /** Positive Z face (+Z direction) */
    PZ
}

/**
 * List of resource paths for texture array elements.
 * All textures must have identical dimensions.
 */
typealias TextureArrayResources = List<String>

/**
 * List of Image objects for texture array elements.
 * All images must have identical dimensions.
 */
typealias TextureArrayImages = List<Image>

/**
 * Map of cubemap faces to resource paths.
 * All faces must be square images of the same size.
 */
typealias CubeTextureResources = Map<CubeTextureSide, String>

/**
 * Map of cubemap faces to Image objects.
 * All faces must be square images of the same size.
 */
typealias CubeTextureImages = Map<CubeTextureSide, Image>
