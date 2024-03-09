package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.material.Image
import java.io.InputStream

interface Platform {

    val name: String

    fun loadImage(stream: InputStream) : Image

    @Composable
    fun openGL(init: () -> Unit, frame: () -> Unit, resize: (Int, Int) -> Unit)
}

expect fun getPlatform(): Platform

