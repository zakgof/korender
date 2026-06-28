package com.zakgof.korender.impl.context

import com.zakgof.korender.TextStyle
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.impl.engine.ElementDeclaration
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.scope.FrameScope
import com.zakgof.korender.scope.GuiContainerScope

internal class DefaultContainerScope(
    private val frameContext: DefaultFrameScope,
    private val declaration: ElementDeclaration.Container,
) : GuiContainerScope, FrameScope by frameContext {

    override fun Row(paddingTop: Float, paddingRight: Float, paddingBottom: Float, paddingLeft: Float, block: GuiContainerScope.() -> Unit) {
        val row = ElementDeclaration.Container(Direction.Horizontal, paddingTop, paddingRight, paddingBottom, paddingLeft)
        DefaultContainerScope(frameContext, row).apply(block)
        declaration.add(row)
    }

    override fun Column(paddingTop: Float, paddingRight: Float, paddingBottom: Float, paddingLeft: Float, block: GuiContainerScope.() -> Unit) {
        val column = ElementDeclaration.Container(Direction.Vertical, paddingTop, paddingRight, paddingBottom, paddingLeft)
        DefaultContainerScope(frameContext, column).apply(block)
        declaration.add(column)
    }

    override fun Stack(paddingTop: Float, paddingRight: Float, paddingBottom: Float, paddingLeft: Float, block: GuiContainerScope.() -> Unit) {
        val stack = ElementDeclaration.Container(Direction.Stack, paddingTop, paddingRight, paddingBottom, paddingLeft)
        DefaultContainerScope(frameContext, stack).apply(block)
        declaration.add(stack)
    }

    override fun Text(id: String, text: String, style: TextStyle?, fontResource: String?, height: Float?, color: ColorRGBA?, static: Boolean, onTouch: TouchHandler) {
        declaration.add(
            ElementDeclaration.Text(
                id,
                fontResource ?: style?.fontResource ?: "!font/anta.ttf",
                height ?: style?.height ?: 32f,
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

    override fun Image(id: String, imageResource: String, width: Float, height: Float, onTouch: TouchHandler) {
        declaration.add(ElementDeclaration.Image(id, imageResource, width, height, onTouch, frameContext.nodeContext))
    }
}

internal enum class Direction {
    Vertical, Horizontal, Stack
}
