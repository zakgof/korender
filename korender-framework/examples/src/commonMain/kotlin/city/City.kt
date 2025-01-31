package com.zakgof.korender.examples.city

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.city.controller.Controller
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import org.jetbrains.compose.resources.ExperimentalResourceApi
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

        if (target == KorenderContext.TargetPlatform.Desktop) {
            AmbientLight(white(0.2f))
        } else {
            AmbientLight(white(0.4f))
        }
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
                    PointLight(Vec3(-192f + 4 + 96f * xx, 8f, -192f + 4 + 96f * zz), white(1.1f))
                }
            }
        }

        staticScene.render(this)
        Scene(gltfResource = "city/swat.glb", time=controller.character.animTime, transform = controller.character.transform * scale(0.002f))

        Gui {
            Text(
                id = "fps",
                text = "FPS ${frameInfo.avgFps.toInt()}",
                fontResource = "font/anta.ttf",
                height = 25,
                color = Color(0xFF66FF55)
            )
            Filler()
            Row {
                joystick.render(this)
                Filler()
            }
        }

        controller.joystick(joystick.offset, frameInfo.dt)

    }
}