package com.zakgof.korender

import com.zakgof.korender.context.GuiContainerContext
import com.zakgof.korender.math.ColorRGBA
import kotlin.math.sqrt

data class TextStyle (
    val fontResource: String = "!font/anta.ttf",
    val color: ColorRGBA = ColorRGBA(0x66FF55A0),
    val height: Int = 32
)

class CheckboxState(initialState: Boolean = false) {
    var state = initialState
}

class JoystickState {
    var x: Float = 0f
    var y: Float = 0f
    internal var downEvent: TouchEvent? = null
    internal var touchX: Float = 0f
    internal var touchY: Float = 0f
}

fun GuiContainerContext.Checkbox(id: String, state: CheckboxState, text: String? = null, onChange: (Boolean) -> Unit = {}) =
    Row {
        val clickHandler: (TouchEvent) -> Unit = {
            onClick(it) {
                state.state = !state.state
                onChange.invoke(state.state)
            }
        }
        text?.let {
            Text(id = "checkbox.label.$id", fontResource = "!font/anta.ttf", height = 48, text = it, color = ColorRGBA(0x66FF55A0), onTouch = clickHandler)
        }
        Image(id = "checkbox.image.$id.${state.state}", imageResource = if (state.state) "!gui/checkbox.checked.png" else "!gui/checkbox.unchecked.png", width = 48, height = 48, marginLeft = 8, onTouch = clickHandler)
    }

fun GuiContainerContext.ProgressBar(id: String, width: Int, value: Float) =
    Row {
        Image(id = "progressbar.left.$id", imageResource = "!gui/progressbar.filled.png", width = 8, height = 48, marginLeft = 8)
        Image(id = "progressbar.filled.$id", imageResource = "!gui/progressbar.filled.png", width = (value * width).toInt(), height = 48)
        Image(id = "progressbar.empty.$id", imageResource = "!gui/progressbar.empty.png", width = ((1f - value) * width).toInt(), height = 48)
        Image(id = "progressbar.right", imageResource = "!gui/progressbar.filled.png", width = 8, height = 48)
    }

fun GuiContainerContext.Joystick(id: String, state: JoystickState, width: Int) {

    val unit = width / 8
    Stack {
        Image(
            id = "joystick.outer.$id",
            imageResource = "!gui/joystick.outer.png",
            width = unit * 8,
            height = unit * 8,
            marginLeft = unit,
            marginBottom = unit
        )
        Image(
            id = "joystick.inner.$id",
            imageResource = "!gui/joystick.inner.png",
            width = unit * 4,
            height = unit * 4,
            marginLeft = unit * 3 + (state.x * unit).toInt(),
            marginTop = unit * 2 + (state.y * unit).toInt(),
            onTouch = { touch ->
                if (touch.type == TouchEvent.Type.DOWN) {
                    state.downEvent = touch
                    state.touchX = 0f
                    state.touchY = 0f
                }
                if (touch.type == TouchEvent.Type.UP) {
                    state.downEvent = null
                    state.touchX = 0f
                    state.touchY = 0f
                }
                if (touch.type == TouchEvent.Type.MOVE && state.downEvent != null) {
                    state.touchX = (1.0f * (touch.x - state.downEvent!!.x) / unit)
                    state.touchY = (1.0f * (touch.y - state.downEvent!!.y) / unit)
                }

                val l2 = state.touchX * state.touchX + state.touchY * state.touchY
                if (l2 > 4.0) {
                    val l = 2.0f / sqrt(l2)
                    state.touchX *= l
                    state.touchY *= l
                }
                state.x = state.touchX
                state.y = state.touchY
            })
    }

}
