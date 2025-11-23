package com.zakgof.korender.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun SsrExample() = Column {
    val orbitCamera = remember { OrbitCamera(20.z + 10.y) }
    SsrDemo(orbitCamera, ssr = false)
    SsrDemo(orbitCamera, ssr = true)
}

@Composable
fun ColumnScope.SsrDemo(orbitCamera: OrbitCamera, ssr: Boolean) = Box(modifier = Modifier.weight(1f)) {
    Korender(appResourceLoader = { Res.readBytes(it) }) {

        OnTouch { orbitCamera.touch(it) }
        val env = cubeTexture(CubeTextureSide.entries.associateWith { "cube/room/${it.toString().lowercase()}.jpg" })
        Frame {
            camera = orbitCamera.run { camera() }
            DeferredShading {
                if (ssr) {
                    PostShading(ssr(downsample = 3, envTexture = env))
                } else {
                    Shading(ibl(env))
                }
            }
            DirectionalLight(Vec3(1f, -1f, 0f))
            Sky(cubeSky(env))
            repeat(5) {
                Renderable(
                    base(
                        colorTexture = texture("texture/brick.jpg"),
                        metallicFactor = 0f,
                        roughnessFactor = 0.2f
                    ),
                    mesh = sphere(),
                    transform = translate(-8f + it * 4, -1f, -4f),
                    transparent = false
                )
                Renderable(
                    base(
                        colorTexture = texture("texture/asphalt-albedo.jpg"),
                        metallicFactor = 0.8f,
                        roughnessFactor = 0.2f * it
                    ),
                    triplanar(0.4f),
                    mesh = cube(1.5f),
                    transform = scale(1f, 1f, 4f).translate(Vec3(-8f + it * 4, -3.5f, -2f))
                )
            }
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }
}

