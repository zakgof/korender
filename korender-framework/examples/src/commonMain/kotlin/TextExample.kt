package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.korender.Bucket
import com.zakgof.korender.Korender
import com.zakgof.korender.font.Fonts
import com.zakgof.korender.math.Color
import com.zakgof.korender.projection.FrustumProjection

@Composable
fun TextExample() = Korender {

    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    val font = Fonts.load(gpu, "/ubuntu.ttf")
    val title = font.renderable("Hello Korender", Color(0x3087FF),32.0f / height, 0.0f, (height - 32.0f) / height)
    add(title, Bucket.SCREEN)

    val fps = font.dynamic(gpu,10, Color(0x00FF00))
    add(fps, Bucket.SCREEN)

    onFrame = {frameInfo ->
        fps.update("FPS: " + frameInfo.avgFps.toInt(), 32.0f / height, 0.0f, 0.0f)
    }
}