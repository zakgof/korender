package com.zakgof.korender.scope

import com.zakgof.korender.TextStyle
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.ColorRGBA

interface GuiContainerScope : FrameScope {

    /**
     *  Arranges GUI widgets into a horizontal row.
     *
     *  @param block row's widgets or children layouts
     */
    fun Row(block: GuiContainerScope.() -> Unit)

    /**
     *  Arranges GUI widgets into a vertical column.
     *
     *  @param block column's widgets or children layouts
     */
    fun Column(block: GuiContainerScope.() -> Unit)

    /**
     *  Arranges GUI widgets into a stack (one on top on another).
     *
     *  @param block stack's widgets or children layouts
     */
    fun Stack(block: GuiContainerScope.() -> Unit)

    /**
     *  Renders a text.
     *
     *  @param id unique declaration id
     *  @param text text to render
     *  @param style text style object
     *  @param fontResource font resource file name
     *  @param height font height
     *  @param color font color
     *  @param static set to true for optimization if text string never changes
     *  @param onTouch touch event handler for the text
     */
    fun Text(id: String, text: String, style: TextStyle? = null, fontResource: String? = null, height: Float? = null, color: ColorRGBA? = null, static: Boolean = false, onTouch: TouchHandler = {})

    /**
     * Adds spacing filling all the available space in a row/column.
     */
    fun Filler()

    /**
     *  Renders an image.
     *
     *  @param id unique declaration id
     *  @param imageResource image resource file name
     *  @param width image width, in screen pixels
     *  @param width image height, in screen pixels
     *  @param marginTop margin to add above the image
     *  @param marginBottom margin to add below the image
     *  @param marginLeft margin to add to the left to the image
     *  @param marginRight margin to add to the right to the image
     *  @param onTouch touch event handler for the text
     */
    fun Image(id: String, imageResource: String, width: Float, height: Float, marginTop: Float = 0f, marginBottom: Float = 0f, marginLeft: Float = 0f, marginRight: Float = 0f, onTouch: TouchHandler = {})
}
