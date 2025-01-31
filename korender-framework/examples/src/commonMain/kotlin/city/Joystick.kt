package com.zakgof.korender.examples.city

import com.zakgof.korender.KeyEvent
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.GuiContainerContext
import kotlin.math.min
import kotlin.math.sqrt

class Joystick {

    private var downEvent: TouchEvent? = null
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var keyX: Float = 0f
    private var keyY: Float = 0f

    val offset: Pair<Float, Float>
        get() {
            var xx = keyX + touchX
            var yy = keyY + touchY
            val l2 = xx * xx + yy * yy
            if (l2 > 1.0) {
                val l = 1.0f / sqrt(l2)
                xx *= l
                yy *= l
            }
            return xx to yy
        }


    fun render(ctx: GuiContainerContext) = with(ctx) {

        OnKey { keyEvent ->
            if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "w")
                keyY = -1f
            if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "s")
                keyY = 1f
            if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "w")
                keyY = 0f
            if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "s")
                keyY = 0f
            if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "a")
                keyX = -1f
            if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "a")
                keyX = 0f
            if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.key == "d")
                keyX = 1f
            if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "d")
                keyX = 0f
        }

        val minDim = min(width, height) / 10
        Stack {
            Image(imageResource = "city/joystick-outer.png", width = minDim * 2, height = minDim * 2, marginBottom = minDim / 2, marginLeft = minDim / 2)
            Image(id = "joystick", imageResource = "city/joystick-inner.png", width = minDim, height = minDim, marginLeft = minDim + (offset.first * minDim / 2).toInt(), marginTop = minDim / 2 + (offset.second * minDim / 2).toInt(), onTouch = { touch ->
                if (touch.type == TouchEvent.Type.DOWN) {
                    downEvent = touch
                    touchX = 0f
                    touchY = 0f
                }
                if (touch.type == TouchEvent.Type.UP) {
                    downEvent = null
                    touchX = 0f
                    touchY = 0f
                }
                if (touch.type == TouchEvent.Type.MOVE && downEvent != null) {
                    touchX = (2.0f * (touch.x - downEvent!!.x) / minDim)
                    touchY = (2.0f * (touch.y - downEvent!!.y) / minDim)
                }
            })
        }
    }
}