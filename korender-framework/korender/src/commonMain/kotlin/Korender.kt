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

