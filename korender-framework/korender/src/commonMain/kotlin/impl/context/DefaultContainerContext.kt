package com.zakgof.korender.impl.context

import com.zakgof.korender.TextStyle
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.math.ColorRGBA

internal class DefaultContainerContext(
    private val frameContext: FrameContext,
    private val declaration: ElementDeclaration.Container
) : GuiContainerContext, FrameContext by frameContext {

    override fun Row(block: GuiContainerContext.() -> Unit) {
        val row = ElementDeclaration.Container(Direction.Horizontal)
        DefaultContainerContext(frameContext, row).apply(block)
        declaration.add(row)
    }

    override fun Column(block: GuiContainerContext.() -> Unit) {
        val column = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerContext(frameContext, column).apply(block)
        declaration.add(column)
    }

    override fun Stack(block: GuiContainerContext.() -> Unit) {
        val stack = ElementDeclaration.Container(Direction.Stack)
        DefaultContainerContext(frameContext, stack).apply(block)
        declaration.add(stack)
    }

    override fun Text(id: Any, text: String, style: TextStyle?, fontResource: String?, height: Int?, color: ColorRGBA?, static: Boolean, onTouch: TouchHandler) {
        declaration.add(
            ElementDeclaration.Text(
                id,
                fontResource ?: style?.fontResource ?: "!font/anta.ttf",
                height ?: style?.height ?: 32,
                text,
                color ?: style?.color ?: ColorRGBA(0x66FF55A0),
                static,
                onTouch
            )
        )
    }

    override fun Filler() {
        declaration.add(ElementDeclaration.Filler())
    }

    override fun Image(id: Any?, imageResource: String, width: Int, height: Int, marginTop: Int, marginBottom: Int, marginLeft: Int, marginRight: Int, onTouch: TouchHandler) {
        declaration.add(ElementDeclaration.Image(id, imageResource, width, height, marginTop, marginBottom, marginLeft, marginRight, onTouch))
    }
}

internal enum class Direction {
    Vertical, Horizontal, Stack
}