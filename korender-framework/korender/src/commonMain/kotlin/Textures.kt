package com.zakgof.korender

interface TextureDeclaration

enum class TextureFilter {
    Nearest,
    Linear,
    MipMap,
    MipMapNearestNearest,
    MipMapLinearNearest,
    MipMapNearestLinear,
    MipMapLinearLinear
}

enum class TextureWrap {
    MirroredRepeat,
    ClampToEdge,
    Repeat
}