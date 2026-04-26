package com.zakgof.korender

import com.zakgof.korender.context.GuiContainerScope
import com.zakgof.korender.math.ColorRGBA
import kotlin.math.sqrt

/**
 * Text rendering style configuration.
 * @param fontResource path to font file (default: built-in Anta font)
 * @param color text color with alpha
 * @param height font height in pixels
 */
data class TextStyle(
    val fontResource: String = "!font/anta.ttf",
    val color: ColorRGBA = ColorRGBA(0x66FF55A0),
    val height: Int = 32
)

/**
 * Checkbox GUI widget state.
 * @param initialState initial checked state
 */
class CheckboxState(initialState: Boolean = false) {
    var state = initialState
}

/**
 * Joystick/analog stick GUI widget state.
 * Tracks 2D input from a joystick control.
 */
class JoystickState {
    /** X axis position (-1.0 to 1.0) */
    var x: Float = 0f

    /** Y axis position (-1.0 to 1.0) */
    var y: Float = 0f

    internal var downEvent: TouchEvent? = null
    internal var touchX: Float = 0f
    internal var touchY: Float = 0f
}

/**
 * Slider GUI widget state.
 * Tracks the position of a value slider.
 *
 * @param position current slider position (between min and max)
 * @param min minimum slider value
 * @param max maximum slider value
 */
class SliderState(var position: Float = 0.5f, val min: Float = 0f, val max: Float = 1f) {
    internal var dragStartX: Float? = null
    internal var dragStartPos: Float? = null
}

/**
 * Checkbox GUI component.
 * Renders a checkbox with optional label and state change callback.
 *
 * @param id unique identifier for the widget
 * @param state checkbox state
 * @param text optional label text
 * @param onChange callback when checkbox state changes
 */
fun GuiContainerScope.Checkbox(id: String, state: CheckboxState, text: String? = null, onChange: (Boolean) -> Unit = {}) =
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

/**
 * Progress bar GUI component.
 * Displays a horizontal progress bar showing a value from 0.0 to 1.0.
 *
 * @param id unique identifier for the widget
 * @param width bar width in pixels
 * @param height bar height in pixels (default: 48)
 * @param value progress value (0.0 = empty, 1.0 = full)
 */
fun GuiContainerScope.ProgressBar(id: String, width: Int, height: Int = 48, value: Float) =
    Row {
        Image(id = "progressbar.left.$id", imageResource = "!gui/progressbar.filled.png", width = 8, height = height, marginLeft = 8)
        Image(id = "progressbar.filled.$id", imageResource = "!gui/progressbar.filled.png", width = (value * width).toInt(), height = height)
        Image(id = "progressbar.empty.$id", imageResource = "!gui/progressbar.empty.png", width = ((1f - value) * width).toInt(), height = height)
        Image(id = "progressbar.right.$id", imageResource = "!gui/progressbar.filled.png", width = 8, height = height)
    }

/**
 * Slider GUI component.
 * Horizontal slider control for selecting a value within a range.
 *
 * @param id unique identifier for the widget
 * @param width slider width in pixels
 * @param height slider height in pixels (default: 48)
 * @param state slider state
 * @param onChange callback when slider position changes
 */
fun GuiContainerScope.Slider(id: String, width: Int, height: Int = 48, state: SliderState, onChange: (Float) -> Unit = {}) =
    Row {
        val setPosition = { p: Float ->
            state.position = p.coerceIn(state.min, state.max)
            onChange(state.position)
        }
        val jumpLeft = { setPosition(state.position - (state.max - state.min) * 0.1f) }
        val jumpRight = { setPosition(state.position + (state.max - state.min) * 0.1f) }
        val fillLeft = ((state.position - state.min) * (width - 96) / (state.max - state.min)).toInt()
        val fillRight = width - 96 - fillLeft
        Image(id = "slider.left.$id", imageResource = "!gui/slider.left.png", width = 32, height = height, marginLeft = 8) { onClick(it) { jumpLeft() } }
        Image(id = "slider.left.empty.$id", imageResource = "!gui/slider.empty.png", width = fillLeft, height = height) { onClick(it) { jumpLeft() } }
        Image(id = "slider.handle.$id", imageResource = "!gui/slider.handle.png", width = 32, height = height) { te ->
            when (te.type) {
                TouchEvent.Type.DOWN -> {
                    state.dragStartX = te.x
                    state.dragStartPos = state.position
                }
                TouchEvent.Type.UP -> {
                    state.dragStartX = null
                    state.dragStartPos = null
                }
                TouchEvent.Type.MOVE -> {
                    state.dragStartX?.let {
                        state.position = (state.dragStartPos!! + (te.x - it) * (state.max - state.min) / (width - 96)).coerceIn(state.min, state.max)
                    }
                }
            }
        }
        Image(id = "slider.right.empty.$id", imageResource = "!gui/slider.empty.png", width = fillRight, height = height) { onClick(it) { jumpRight() } }
        Image(id = "slider.right.$id", imageResource = "!gui/slider.right.png", width = 32, height = height, marginRight = 8) { onClick(it) { jumpRight() } }
    }

fun GuiContainerScope.Joystick(id: String, state: JoystickState, width: Int) {

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
            marginLeft = unit * 3 + (state.x * 1.8f * unit).toInt(),
            marginTop = unit * 2 + (-state.y * 1.8f * unit).toInt(),
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
                    state.touchX = (0.5f * (touch.x - state.downEvent!!.x) / unit)
                    state.touchY = (-0.5f * (touch.y - state.downEvent!!.y) / unit)
                }

                val l2 = state.touchX * state.touchX + state.touchY * state.touchY
                if (l2 > 1.0) {
                    val l = 1.0f / sqrt(l2)
                    state.touchX *= l
                    state.touchY *= l
                }
                state.x = state.touchX
                state.y = state.touchY
            })
    }

}
