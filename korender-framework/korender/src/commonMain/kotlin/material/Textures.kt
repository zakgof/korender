package com.zakgof.korender.material

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