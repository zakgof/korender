package com.zakgof.korender

class TouchEvent(val type: Type, val x: Float, val y: Float) {

    enum class Type {
        UP,
        DOWN,
        MOVE
    }
}

typealias TouchHandler = (TouchEvent) -> Unit

fun onClick(touchEvent: TouchEvent, clickHandler: () -> Unit) {
    if (touchEvent.type == TouchEvent.Type.DOWN) {
        clickHandler()
    }
}