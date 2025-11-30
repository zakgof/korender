package com.zakgof.korender

interface TextureDeclaration

interface Texture3DDeclaration

interface CubeTextureDeclaration

enum class TextureFilter {
    Nearest,
    Linear,
    MipMap
}

enum class TextureWrap {
    MirroredRepeat,
    ClampToEdge,
    Repeat
}

enum class CubeTextureSide {
    NX, NY, NZ, PX, PY, PZ
}

typealias CubeTextureResources = Map<CubeTextureSide, String>

typealias CubeTextureImages = Map<CubeTextureSide, Image>