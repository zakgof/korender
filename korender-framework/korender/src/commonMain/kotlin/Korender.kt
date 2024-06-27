package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.declaration.KorenderContext
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.input.TouchEvent

@Composable
fun Korender(block: KorenderContext.() -> Unit) {

    lateinit var engine: Engine

    getPlatform().openGL(
        init = { w, h -> engine = Engine(w, h, block) },
        frame = { engine.frame() },
        resize = { w, h -> engine.resize(w, h) },
        touch = { e -> engine.pushTouch(e) }
    )
}

typealias TouchHandler = (TouchEvent) -> Unit

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}

