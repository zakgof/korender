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

enum class ShaderPluginId {
    TEXSOURCE,
    TEXTURING,
    ALBEDO,
    DEPTH,
    DISCARD,
    EMISSION,
    METALLIC_ROUGHNESS,
    NORMAL,
    OCCLUSION,
    OUTPUT,
    POSITION,
    SECSKY,
    SKY,
    SPECULAR_GLOSSINESS,
    TERRAIN,
    VNORMAL,
    VPOSITION,
    VPROJECTION;

    val id = ordinal
    val key = toString().lowercase()
}
