package com.zakgof.korender.scope

import com.zakgof.korender.TextStyle
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.ColorRGBA

/**
 * Scope for declaring GUI layouts (rows, columns, stacks) and drawing widgets like text or images.
 */
interface GuiContainerScope : FrameScope {

    /**
     *  Arranges GUI widgets into a horizontal row.
     *
     *  @param paddingTop padding above the row
     *  @param paddingRight padding to the right of the row
     *  @param paddingBottom padding below the row
     *  @param paddingLeft padding to the left of the row
     *  @param block row's widgets or children layouts
     */
    fun Row(paddingTop: Float = 0f, paddingRight: Float = 0f, paddingBottom: Float = 0f, paddingLeft: Float = 0f, block: GuiContainerScope.() -> Unit)

    /**
     *  Arranges GUI widgets into a horizontal row with uniform padding.
     *
     *  @param padding uniform padding on all sides
     *  @param block row's widgets or children layouts
     */
    fun Row(padding: Float, block: GuiContainerScope.() -> Unit) = Row(paddingLeft = padding, paddingRight = padding, paddingTop = padding, paddingBottom = padding, block = block)

    /**
     *  Arranges GUI widgets into a vertical column.
     *
     *  @param paddingTop padding above the column
     *  @param paddingRight padding to the right of the column
     *  @param paddingBottom padding below the column
     *  @param paddingLeft padding to the left of the column
     *  @param block column's widgets or children layouts
     */
    fun Column(paddingTop: Float = 0f, paddingRight: Float = 0f, paddingBottom: Float = 0f, paddingLeft: Float = 0f, block: GuiContainerScope.() -> Unit)

    /**
     *  Arranges GUI widgets into a vertical column with uniform padding.
     *
     *  @param padding uniform padding on all sides
     *  @param block column's widgets or children layouts
     */
    fun Column(padding: Float, block: GuiContainerScope.() -> Unit) = Column(paddingLeft = padding, paddingRight = padding, paddingTop = padding, paddingBottom = padding, block = block)

    /**
     *  Arranges GUI widgets into a stack (one on top on another).
     *
     *  @param paddingTop padding above the stack
     *  @param paddingRight padding to the right of the stack
     *  @param paddingBottom padding below the stack
     *  @param paddingLeft padding to the left of the stack
     *  @param block stack's widgets or children layouts
     */
    fun Stack(paddingTop: Float = 0f, paddingRight: Float = 0f, paddingBottom: Float = 0f, paddingLeft: Float = 0f, block: GuiContainerScope.() -> Unit)

    /**
     *  Arranges GUI widgets into a stack with uniform padding.
     *
     *  @param padding uniform padding on all sides
     *  @param block stack's widgets or children layouts
     */
    fun Stack(padding: Float, block: GuiContainerScope.() -> Unit) = Stack(paddingLeft = padding, paddingRight = padding, paddingTop = padding, paddingBottom = padding, block = block)

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
     *  @param height image height, in screen pixels
     *  @param onTouch touch event handler for the image
     */
    fun Image(id: String, imageResource: String, width: Float, height: Float, onTouch: TouchHandler = {})
}
