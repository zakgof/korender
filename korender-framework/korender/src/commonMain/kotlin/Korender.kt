package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.context.KorenderScope

@Composable
expect fun Korender(
    resourceLoader: ResourceLoader = { throw KorenderException("No application resource provided") },
    vSync: Boolean = false,
    block: KorenderScope.() -> Unit
)

typealias ResourceLoader = suspend (String) -> ByteArray

class KorenderException(message: String) : RuntimeException(message)

class FrameInfo(
    val frame: Long,
    val time: Float,
    val dt: Float,
    val avgFps: Float,
) {
    constructor() : this(0L, 0f, 0f, 0f)
}

interface RetentionPolicy

interface ShaderPlugin

enum class ShaderPluginId(val id: Int, val key: String) {
    TEXTURING(0, "texturing"),
    ALBEDO(1, "albedo"),
    DEPTH(2, "depth"),
    DISCARD(3, "discard"),
    EMISSION(4, "emission"),
    METALLIC_ROUGHNESS(5, "metallic_roughness"),
    NORMAL(6, "normal"),
    OCCLUSION(7, "occlusion"),
    OUTPUT(8, "output"),
    POSITION(9, "position"),
    SECSKY(10, "secsky"),
    SKY(11, "sky"),
    SPECULAR_GLOSSINESS(12, "specular_glossiness"),
    TERRAIN(13, "terrain"),
    VNORMAL(14, "vnormal"),
    VPOSITION(15, "vposition"),
    VPROJECTION(16, "vprojection"),
}
