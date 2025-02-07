package com.zakgof.korender

interface TextureDeclaration

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