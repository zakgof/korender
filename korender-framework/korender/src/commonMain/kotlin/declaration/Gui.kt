package com.zakgof.korender.declaration

import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Color

class ContainerContext internal constructor(private val declaration: ElementDeclaration.Container) {
    fun Row(block: ContainerContext.() -> Unit) {
        val row = ElementDeclaration.Container(Direction.Horizontal)
        ContainerContext(row).apply(block)
        declaration.add(row)
    }

    fun Column(block: ContainerContext.() -> Unit) {
        val column = ElementDeclaration.Container(Direction.Vertical)
        ContainerContext(column).apply(block)
        declaration.add(column)
    }

    fun Text(
        id: Any,
        fontResource: String,
        height: Int,
        text: String,
        color: Color,
        onTouch: TouchHandler = {}
    ) = declaration.add(ElementDeclaration.Text(id, fontResource, height, text, color, onTouch))

    fun Filler() = declaration.add(ElementDeclaration.Filler())

    fun Image(imageResource: String, width: Int, height: Int, onTouch: TouchHandler = {}) = declaration.add(ElementDeclaration.Image(imageResource, width, height, onTouch))

}

enum class Direction {
    Vertical,
    Horizontal
}

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}