package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.input.TouchEvent
import java.io.InputStream

interface Platform {

    val name: String

    fun loadImage(stream: InputStream) : Image

    fun loadFont(stream: InputStream): FontDef

    @Composable
    fun openGL(
        init: (Int, Int) -> Unit,
        frame: () -> Unit,
        resize: (Int, Int) -> Unit,
        touch: (touchEvent: TouchEvent) -> Unit
    )

}

expect fun getPlatform(): Platform

