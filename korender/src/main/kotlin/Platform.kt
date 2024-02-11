package com.zakgof.korender

interface Platform {

    fun run(
        width: Int,
        height: Int,
        init: () -> Unit = {},
        onFrame: () -> Unit = {},
        onResize: (Int, Int) -> Unit = {x,y->{}}
    )
}