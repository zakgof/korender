package com.zakgof.korender.declaration

object Textures {

    // TODO: wrap and filter
    fun texture(textureResource: String): TextureDeclaration = TextureDeclaration(textureResource)
}

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

data class TextureDeclaration(val textureResource: String) // TODO filter and wrap