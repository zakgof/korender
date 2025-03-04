package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.context.KorenderContext

@Composable
expect fun Korender(
    appResourceLoader: ResourceLoader = { throw KorenderException("No application resource provided") },
    block: KorenderContext.() -> Unit
)

typealias ResourceLoader = suspend (String) -> ByteArray

class KorenderException(message: String) : RuntimeException(message)

class FrameInfo(
    val frame: Long,
    val time: Float,
    val dt: Float,
    val avgFps: Float,
)

interface Prefab {

}