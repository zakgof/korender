package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.Slider
import com.zakgof.korender.SliderState
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.White
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z

@Composable
fun DetailTexturingExample() {
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        val orbitCamera = OrbitCamera(15.z, 0.z)
        val plane = customMesh("plane", 4, 6, POS, TEX) {
            pos(Vec3(-2f, -2f, 0f)).tex(Vec2(0f, 0f))
            pos(Vec3(2f, -2f, 0f)).tex(Vec2(2f, 0f))
            pos(Vec3(2f, 2f, 0f)).tex(Vec2(2f, 2f))
            pos(Vec3(-2f, 2f, 0f)).tex(Vec2(0f, 2f))
            index(0, 1, 2, 0, 2, 3)
        }
        val scaleSlider = SliderState(3f, 0.5f, 8f)
        val strengthSlider = SliderState(0.6f, 0f, 4f)
        OnTouch { orbitCamera.touch(it) }
        Frame {
            AmbientLight(White)
            camera = orbitCamera.run { camera() }

            val baseMaterial = base {
                colorTexture = texture("texture/brick.jpg")
                metallicFactor = 0.3f
                roughnessFactor = 0.5f
                detailTexture {
                    texture = texture("!texture/noise.png")
                    scale = scaleSlider.position
                    strength = strengthSlider.position
                }
            }

            Renderable(
                material = baseMaterial,
                mesh = plane
            )

            Gui {
                Row {
                    Column {
                        Filler()
                        Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                    }
                    Filler()
                    Column {
                        Row {
                            Filler()
                            Text(id = "scale1", height = 48f, text = "Scale ${scaleSlider.position.fixedDecimals(1)} ")
                            Slider("scale2", scaleSlider, width / 2f, 64f)
                        }
                        Row {
                            Filler()
                            val s = strengthSlider.position.fixedDecimals(1)
                            Text(id = "strength1", height = 48f, text = "Strength $s ")
                            Slider("strength2", strengthSlider, width / 2f, 64f)
                        }
                    }
                }
            }
            TestExchange.report(frameInfo)
        }
    }
}
