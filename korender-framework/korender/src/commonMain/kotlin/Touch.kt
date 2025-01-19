package com.zakgof.korender

class TouchEvent(val type: Type, val x: Float, val y: Float) {

    enum class Type {
        UP,
        DOWN,
        MOVE
    }
}

class KeyEvent(val type: Type, val key: String) {
    enum class Type {
        UP,
        DOWN
    }
}

typealias TouchHandler = (TouchEvent) -> Unit
typealias KeyHandler = (KeyEvent) -> Unit

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}