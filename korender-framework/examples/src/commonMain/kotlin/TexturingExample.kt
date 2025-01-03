package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TexturingExample(): Unit = Korender(appResourceLoader = { Res.readBytes(it) }) {
    camera = camera(position = 20.z, direction = -1.z, up = 1.y)
    Frame {
        Renderable(
            standart {
                baseColorTexture = texture("sand.jpg")
            },
            mesh = sphere(2f),
            transform = rotate(1.y, frameInfo.time * 0.1f).translate(-2.1f.x)
        )
//  TODO      Renderable(
//            standart(StandartMaterialOption.Triplanar) {
//                baseColorTexture = texture("!sand.jpg")
//                triplanarScale = 0.1f
//            },
//            mesh = sphere(2f),
//            transform = rotate(1.y, frameInfo.time * 0.1f).translate(2.1f.x)
//        )
    }
}