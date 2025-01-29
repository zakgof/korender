package com.zakgof.korender.context

import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.Color

interface GuiContainerContext {
    fun Row(block: GuiContainerContext.() -> Unit)
    fun Column(block: GuiContainerContext.() -> Unit)
    fun Text(id: Any, fontResource: String, height: Int, text: String, color: Color, static: Boolean = false, onTouch: TouchHandler = {})
    fun Filler()
    fun Image(imageResource: String, width: Int, height: Int, marginTop: Int = 0, marginBottom: Int = 0, marginLeft: Int = 0, marginRight: Int = 0, onTouch: TouchHandler = {})
}