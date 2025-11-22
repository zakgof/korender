package com.zakgof.korender.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec3

@Composable
fun MultipleViewportsExample() = Column(
    verticalArrangement = Arrangement.spacedBy(5.dp)
) {
    Box (modifier = Modifier.weight(1f)) {
        Korender(appResourceLoader = { Res.readBytes(it) }) {
            Frame {
                DirectionalLight(Vec3(1f, -1f, 0f))
                Renderable(
                    base(color = ColorRGBA.Red),
                    mesh = sphere(1f)
                )
            }
        }
    }
    Box (modifier = Modifier.weight(1f)) {
        Korender(appResourceLoader = { Res.readBytes(it) }) {
            Frame {
                DirectionalLight(Vec3(1f, -1f, 0f))
                Renderable(
                    base(color = ColorRGBA.Green),
                    mesh = sphere(1f)
                )
            }
        }
    }
}
