package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Checkbox
import com.zakgof.korender.CheckboxState
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA.Companion.Red
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

@Composable
fun CSMExample() =
    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        val material = base {
            colorTexture = texture("texture/asphalt-albedo.jpg")
            metallicFactor = 0f
            roughnessFactor = 0.9f
            triplanarScale = 1.0f
        }
        val freeCamera = FreeCamera(this, Vec3(0f, 4f, 5f), (-1).z)
        OnTouch { freeCamera.touch(it) }
        OnKey { freeCamera.handle(it) }

        val cascade1checkbox = CheckboxState(true)
        val cascade2checkbox = CheckboxState(true)
        val cascade3checkbox = CheckboxState(true)

        Frame {

            TestExchange.report(frameInfo)

            projection = projection(4f * width / height, 4f, 4f, 10000f)
            camera = freeCamera.camera(projection, width, height, frameInfo.dt)
            DirectionalLight(Vec3(1f, -1f, 0.3f), white(3.0f)) {
                if (cascade1checkbox.state)
                    Cascade(1024, 4f, 12f, -0f to 50f, softwarePcf(blurRadius = 0.02f, bias = 0.001f))
                if (cascade2checkbox.state)
                    Cascade(1024, 10f, 30f, 0f to 50f, vsm())
                if (cascade3checkbox.state)
                    Cascade(1024, 25f, 100f, 0f to 50f, vsm())
            }
            Renderable(
                material,
                mesh = cube(1f),
                transform = scale(1000f, 1f, 1000f).translate(-1.y)
            )
            for (i in 0..200) {
                Renderable(
                    material,
                    mesh = sphere(0.5f),
                    transform = translate(Vec3(-3f, 3f, -i * 2f)),
                )
            }

            Renderable(
                material,
                mesh = cube(),
                transform = scale(1f, 1f, 200f).translate(Vec3(-5f, 3f, -100f))
            )
            Renderable(
                material,
                mesh = cube(),
                transform = scale(2f, 10f, 2f).translate(Vec3(-7f, 5f, -10f))
            )
            Renderable(
                base { color = Red },
                mesh = sphere(0.1f),
                transform = translate((-10 + 5).z),
            )
            Renderable(
                base { color = Red },
                mesh = sphere(0.1f),
                transform = translate((-12 + 5).z),
            )
            Renderable(
                base { color = Red },
                mesh = sphere(0.1f),
                transform = translate((-25 + 5).z),
            )
            Renderable(
                base { color = Red },
                mesh = sphere(0.1f),
                transform = translate((-30 + 5).z),
            )
            Renderable(
                base { color = Red },
                mesh = sphere(0.5f),
                transform = translate(2f, 0.5f, -3f),
            )
            Gui {
                Row {
                    Column {
                        Filler()
                        Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                    }
                    Filler()
                    Column {
                        Filler()
                        Column {
                            Checkbox(id = "c1", state = cascade1checkbox, text = "Cascade 1")
                            Checkbox(id = "c2", state = cascade2checkbox, text = "Cascade 2")
                            Checkbox(id = "c3", state = cascade3checkbox, text = "Cascade 3")
                        }
                    }
                }
            }
        }
    }
