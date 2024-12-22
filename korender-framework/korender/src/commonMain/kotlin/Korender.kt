package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.input.TouchEvent

@Composable
expect fun Korender(
    appResourceLoader: ResourceLoader = { throw KorenderException("No application resource provided") },
    block: KorenderContext.() -> Unit
)

typealias ResourceLoader = suspend (String) -> ByteArray

typealias TouchHandler = (TouchEvent) -> Unit

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}

