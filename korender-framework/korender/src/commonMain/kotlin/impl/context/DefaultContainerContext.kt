package com.zakgof.korender.impl.context

import com.zakgof.korender.TouchHandler
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.math.Color

internal class DefaultContainerContext(private val declaration: ElementDeclaration.Container) : GuiContainerContext {

    override fun Row(block: GuiContainerContext.() -> Unit) {
        val row = ElementDeclaration.Container(Direction.Horizontal)
        DefaultContainerContext(row).apply(block)
        declaration.add(row)
    }

    override fun Column(block: GuiContainerContext.() -> Unit) {
        val column = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(column).apply(block)
        declaration.add(column)
    }

    override fun Text(id: Any, fontResource: String, height: Int, text: String, color: Color, static: Boolean, onTouch: TouchHandler) {
        declaration.add(ElementDeclaration.Text(id, fontResource, height, text, color, static, onTouch))
    }

    override fun Filler() {
        declaration.add(ElementDeclaration.Filler())
    }

    override fun Image(imageResource: String, width: Int, height: Int, marginTop: Int, marginBottom: Int, marginLeft: Int, marginRight: Int, onTouch: TouchHandler) {
        declaration.add(ElementDeclaration.Image(imageResource, width, height, marginTop, marginBottom, marginLeft, marginRight, onTouch))
    }
}

internal enum class Direction {
    Vertical, Horizontal
}