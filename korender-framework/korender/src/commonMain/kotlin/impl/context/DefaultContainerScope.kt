package com.zakgof.korender.impl.context

import com.zakgof.korender.TextStyle
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.context.FrameScope
import com.zakgof.korender.context.GuiContainerScope
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.math.ColorRGBA

internal class DefaultContainerScope(
    private val frameContext: DefaultFrameScope,
    private val declaration: ElementDeclaration.Container
) : GuiContainerScope, FrameScope by frameContext {

    override fun Row(block: GuiContainerScope.() -> Unit) {
        val row = ElementDeclaration.Container(Direction.Horizontal)
        DefaultContainerScope(frameContext, row).apply(block)
        declaration.add(row)
    }

    override fun Column(block: GuiContainerScope.() -> Unit) {
        val column = ElementDeclaration.Container(Direction.Vertical)
        DefaultContainerScope(frameContext, column).apply(block)
        declaration.add(column)
    }

    override fun Stack(block: GuiContainerScope.() -> Unit) {
        val stack = ElementDeclaration.Container(Direction.Stack)
        DefaultContainerScope(frameContext, stack).apply(block)
        declaration.add(stack)
    }

    override fun Text(id: String, text: String, style: TextStyle?, fontResource: String?, height: Int?, color: ColorRGBA?, static: Boolean, onTouch: TouchHandler) {
        declaration.add(
            ElementDeclaration.Text(
                id,
                fontResource ?: style?.fontResource ?: "!font/anta.ttf",
                height ?: style?.height ?: 32,
                text,
                color ?: style?.color ?: ColorRGBA(0x66FF55A0),
                static,
                onTouch,
                frameContext.nodeContext
            )
        )
    }

    override fun Filler() {
        declaration.add(ElementDeclaration.Filler())
    }

    override fun Image(id: String, imageResource: String, width: Int, height: Int, marginTop: Int, marginBottom: Int, marginLeft: Int, marginRight: Int, onTouch: TouchHandler) {
        declaration.add(ElementDeclaration.Image(id, imageResource, width, height, marginTop, marginBottom, marginLeft, marginRight, onTouch, frameContext.nodeContext))
    }
}

internal enum class Direction {
    Vertical, Horizontal, Stack
}