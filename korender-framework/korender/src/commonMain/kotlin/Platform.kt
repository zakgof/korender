package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.image.Image
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.input.TouchEvent

interface Platform {

    val name: String

    fun loadImage(bytes: ByteArray) : Image

    fun loadFont(bytes: ByteArray): FontDef

    @Composable
    fun OpenGL(
        init: (Int, Int) -> Unit,
        frame: () -> Unit,
        resize: (Int, Int) -> Unit,
        touch: (touchEvent: TouchEvent) -> Unit
    )

    fun nanoTime(): Long
}

expect fun getPlatform(): Platform

