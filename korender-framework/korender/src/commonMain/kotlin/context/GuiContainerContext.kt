package com.zakgof.korender.context

import com.zakgof.korender.TextStyle
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.ColorRGBA

interface GuiContainerContext : FrameContext {
    fun Row(block: GuiContainerContext.() -> Unit)
    fun Column(block: GuiContainerContext.() -> Unit)
    fun Stack(block: GuiContainerContext.() -> Unit)
    fun Text(id: String, text: String, style: TextStyle? = null, fontResource: String? = null, height: Int? = null, color: ColorRGBA? = null, static: Boolean = false, onTouch: TouchHandler = {})
    fun Filler()
    fun Image(id: String, imageResource: String, width: Int, height: Int, marginTop: Int = 0, marginBottom: Int = 0, marginLeft: Int = 0, marginRight: Int = 0, onTouch: TouchHandler = {})
}