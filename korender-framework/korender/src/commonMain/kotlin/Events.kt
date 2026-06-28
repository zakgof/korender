package com.zakgof.korender

import androidx.compose.ui.input.key.Key

/**
 * Touch/mouse input event.
 * @param type event type (UP, DOWN, MOVE)
 * @param button which button triggered the event (LEFT, RIGHT, NONE for movement)
 * @param x X coordinate in viewport pixels
 * @param y Y coordinate in viewport pixels
 * @param keyboardModifiers keyboard modifier state (shift, ctrl, alt, meta)
 */
class TouchEvent(
    val type: Type,
    val button: Button,
    val x: Float,
    val y: Float,
    val keyboardModifiers: KeyboardModifiers,
) {

    /**
     * Touch event type.
     */
    enum class Type {
        /** Button pressed or touch started */
        UP,
        /** Button released or touch ended */
        DOWN,
        /** Pointer moved */
        MOVE
    }

    /**
     * Mouse/touch button.
     */
    enum class Button {
        /** No button (used for movement events) */
        NONE,
        /** Left mouse button or primary touch */
        LEFT,
        /** Right mouse button or secondary touch */
        RIGHT
    }
}

/**
 * Keyboard input event.
 * @param type event type (UP, DOWN)
 * @param key human-readable key name (e.g., "a", "Enter", "Space")
 * @param composeKey Compose Key enum value
 * @param keyboardModifiers keyboard modifier state
 */
class KeyEvent(
    val type: Type,
    val key: String,
    val composeKey: Key,
    val keyboardModifiers: KeyboardModifiers,
) {
    /**
     * Keyboard event type.
     */
    enum class Type {
        /** Key pressed */
        DOWN,
        /** Key released */
        UP
    }
}

/**
 * Keyboard modifier keys state.
 * @param shiftPressed true if Shift key is held
 * @param ctrlPressed true if Ctrl/Cmd key is held
 * @param altPressed true if Alt/Option key is held
 * @param metaPressed true if Meta/Windows key is held
 */
class KeyboardModifiers(
    val shiftPressed: Boolean = false,
    val ctrlPressed: Boolean = false,
    val altPressed: Boolean = false,
    val metaPressed: Boolean = false,
)

/**
 * Function type for handling touch/mouse events.
 */
typealias TouchHandler = (TouchEvent) -> Unit

/**
 * Function type for handling keyboard events.
 */
typealias KeyHandler = (KeyEvent) -> Unit

/**
 * Helper function to detect click events (touch down).
 * @param touchEvent the touch event to check
 * @param clickHandler function to call if event is a click
 */
fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}