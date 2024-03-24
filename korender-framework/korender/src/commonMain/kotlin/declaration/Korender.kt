package com.zakgof.korender.declaration

import androidx.compose.runtime.Composable
import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.getPlatform
import com.zakgof.korender.input.TouchEvent

@Composable
fun Korender(block: KorenderContext.() -> Unit) {

    lateinit var engine: Engine

    getPlatform().openGL(
        init = { w, h ->
            com.zakgof.korender.impl.gl.VGL11.glEnable(com.zakgof.korender.impl.gl.VGL11.GL_BLEND)
            com.zakgof.korender.impl.gl.VGL11.glEnable(com.zakgof.korender.impl.gl.VGL11.GL_DEPTH_TEST)
            com.zakgof.korender.impl.gl.VGL11.glBlendFunc(com.zakgof.korender.impl.gl.VGL11.GL_SRC_ALPHA, com.zakgof.korender.impl.gl.VGL11.GL_ONE_MINUS_SRC_ALPHA)
            com.zakgof.korender.impl.gl.VGL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            engine = Engine(w, h, block)
        },
        frame = { engine.frame() },
        resize = { w, h -> engine.resize(w, h) },
        touch = { e -> engine.touch(e) }
    )
}

class KorenderContext(private val sceneBlocks: MutableList<SceneContext.() -> Unit>, private val touchHandlers: MutableList<TouchHandler>) {

    fun Scene(block: SceneContext.() -> Unit) {
        if (sceneBlocks.isNotEmpty()) {
            throw KorenderException("Only one Scene is allowed")
        }
        sceneBlocks.add(block)
    }

    fun OnTouch(handler: (TouchEvent) -> Unit) = touchHandlers.add(handler)

}

typealias TouchHandler = (TouchEvent) -> Unit

class SceneDeclaration {

    var gui: ElementDeclaration.ContainerDeclaration? = null
    var shadow: ShadowDeclaration? = null
    val renderables = mutableListOf<RenderableDeclaration>()
    val filters = mutableListOf<FilterDeclaration>()
    fun add(renderable: RenderableDeclaration) = renderables.add(renderable)
    fun add(filter: FilterDeclaration) = filters.add(filter)
    fun addShadow(shadow: ShadowDeclaration) {
        if (this.shadow != null) {
            throw KorenderException("Only one Shadow declaration is allowed")
        }
        this.shadow = shadow
    }
}

