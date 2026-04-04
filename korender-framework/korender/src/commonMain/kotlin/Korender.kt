package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.context.KorenderScope

@Composable
expect fun Korender(
    resourceLoader: ResourceLoader = { throw KorenderException("No application resource provided") },
    block: KorenderScope.() -> Unit
)

typealias ResourceLoader = suspend (String) -> ByteArray

class KorenderException(message: String) : RuntimeException(message)

class FrameInfo(
    val frame: Long,
    val time: Float,
    val dt: Float,
    val avgFps: Float,
    val pending: Int
)

interface Prefab<M : Material>

interface RetentionPolicy