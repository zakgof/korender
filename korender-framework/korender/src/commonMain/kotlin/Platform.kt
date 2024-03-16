package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.font.FontDef
import com.zakgof.korender.material.Image
import java.io.InputStream

interface Platform {

    val name: String

    fun loadImage(stream: InputStream) : Image

    fun loadFont(stream: InputStream): FontDef

    @Composable
    fun openGL(init: (Int, Int) -> Unit, frame: () -> Unit, resize: (Int, Int) -> Unit)

}

expect fun getPlatform(): Platform

