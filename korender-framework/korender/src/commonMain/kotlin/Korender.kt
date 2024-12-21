package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.ResourceLoader
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.input.TouchEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Korender(
    appResourceLoader: ResourceLoader = { throw KorenderException("No application resource provided") },
    block: KorenderContext.() -> Unit
) {
    lateinit var engine: Engine

    getPlatform().OpenGL(
        init = { w, h -> engine = Engine(w, h, appResourceLoader, block) },
        frame = { engine.frame() },
        resize = { w, h -> engine.resize(w, h) },
        touch = { e -> GlobalScope.launch { engine.pushTouch(e) } }
    )
}

typealias TouchHandler = (TouchEvent) -> Unit

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}

