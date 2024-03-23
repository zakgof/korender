package com.zakgof.korender.input

class TouchEvent(val type: Type, val x: Float, val y: Float) {

    enum class Type {
        UP,
        DOWN,
        MOVE
    }
}