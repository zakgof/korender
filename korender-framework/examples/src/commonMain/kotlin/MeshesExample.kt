package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sin

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MeshesExample() {
    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(this, 20.z, 2.y)
        OnTouch { orbitCamera.touch(it) }
        val materialModifier = standart {
            baseColor = ColorRGBA.Blue
        }
        Frame {
            AmbientLight(White)
            camera = orbitCamera.camera(projection, width, height)
            Renderable(
                materialModifier,
                mesh = customMesh(
                    id = "static",
                    vertexCount = 3,
                    indexCount = 3,
                    POS,
                    NORMAL,
                    TEX,
                ) {
                    pos(-5f, 0f, 0f).normal(1.z).tex(0f, 0f)
                    pos(0f, 0f, 0f).normal(1.z).tex(1f, 0f)
                    pos(0f, 5f, 0f).normal(1.z).tex(1f, 1f)
                    index(0, 1, 2)
                }
            )
            Renderable(
                materialModifier,
                mesh = customMesh(
                    id = "dynamic",
                    vertexCount = 3,
                    indexCount = 3,
                    POS, NORMAL, TEX,
                    dynamic = true
                ) {
                    pos(1f, 0f, 0f).normal(1.z).tex(0f, 0f)
                    pos(5f, 0f, 0f).normal(1.z).tex(1f, 0f)
                    pos(5f, 5f + sin(frameInfo.time), 0f).normal(1.z).tex(1f, 1f)
                    index(0, 1, 2)
                }
            )
        }
    }
}