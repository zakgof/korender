package com.zakgof.korender

interface TextureDeclaration

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