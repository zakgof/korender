package com.zakgof.korender

interface Platform {

    var onFrame: () -> Unit
    var onResize: (Int, Int) -> Unit
    var onKey: (KeyEvent) -> Unit

    fun run(
        width: Int,
        height: Int,
        init: () -> Unit = {},

    )

    interface KeyEvent {
        val code: Int
        val press: Boolean
    }
}