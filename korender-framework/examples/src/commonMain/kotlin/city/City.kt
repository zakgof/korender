package com.zakgof.korender.examples.city

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.city.controller.Controller
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.onClick
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.min
import kotlin.random.Random

fun Int.chance(block: () -> Unit) {
    if (Random.nextInt(100) < this) block()
}

@Composable
@OptIn(ExperimentalResourceApi::class)
fun City() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val controller = Controller()
    val staticScene = StaticScene(this, controller)
    val joystick = Joystick()

    Frame {

        DeferredShading(/* plugin("color", "city/composition.color.plugin.frag") */)

        OnTouch { controller.touch(it) }
        projection = frustum(width = 0.3f * width / height, height = 0.3f, near = 0.3f, far = 500f)
        camera = controller.camera(this)

        AmbientLight(white(0.4f))
        DirectionalLight(Vec3(3f, -5f, 6f).normalize(), white(1.0f)) {
            if (target == KorenderContext.TargetPlatform.Desktop) {
                Cascade(1024, 0.3f, 2.8f, -1f to 60f, pcss(8))
                Cascade(1024, 2.0f, 22.0f, -1f to 60f, vsm())
                Cascade(1024, 20.0f, 60.0f, -1f to 60f, vsm())
            } else {
                Cascade(1024, 0.3f, 2.8f, -1f to 60f, hard())
                Cascade(1024, 2.0f, 60.0f, -1f to 60f, vsm())
            }
        }

        if (target == KorenderContext.TargetPlatform.Desktop) {
            for (xx in 0..4) {
                for (zz in 0..4) {
                    PointLight(Vec3(-192f + 96f * xx, 6f, -192f + 96f * zz), white(2f), 0.1f, 0.01f)
                    Gltf(
                        resource = "city/tree.glb",
                        transform = translate(-192f + 96f * xx, 1.48f, -192f + 96f * zz)
                    )
                }
            }
        }

        staticScene.render(this)
        Gltf(
            resource = "city/swat-woman.glb",
            animation = controller.character.animMode,
            time = controller.character.animTime,
            transform = controller.character.transform * scale(0.002f)
        )

        val unit = min(width, height) / 24

        Gui {
            Stack {
                Row {
                    Column {
                        Text(
                            id = "fps",
                            text = "FPS ${frameInfo.avgFps.toInt()}",
                            fontResource = "font/anta.ttf",
                            height = 25,
                            color = ColorRGBA(0x66FF55A0)
                        )
                        Filler()
                        Row {
                            joystick.render(this)
                            Filler()
                        }
                    }
                    Filler()
                    Column {
                        Filler()
                        Image(
                            id = "aim",
                            imageResource = "city/aim.png",
                            width = unit * 4,
                            height = unit * 4,
                            onTouch = { onClick(it) { controller.character.aiming = !controller.character.aiming } }
                        )
                    }
                }
                if (controller.character.aiming) {
                    Image(
                        id = "crosshair",
                        imageResource = "city/aim.png",
                        width = unit,
                        height = unit,
                        marginLeft = (width - unit) / 2,
                        marginTop = ((height * 0.9f - unit) / 2).toInt(),
                    )
                }
            }
        }
        controller.update(joystick.offset, frameInfo.dt)
    }
}