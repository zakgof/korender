package com.zakgof.korender.material

object Textures {
    fun texture(textureResource: String, filter: TextureFilter = TextureFilter.MipMapLinearLinear, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration = TextureDeclaration(textureResource, filter, wrap, aniso)
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

data class TextureDeclaration internal constructor(val textureResource: String, val filter: TextureFilter = TextureFilter.MipMapLinearLinear, val wrap: TextureWrap = TextureWrap.Repeat, val aniso: Int = 1024)