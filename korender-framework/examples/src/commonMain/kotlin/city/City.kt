package com.zakgof.korender.examples.city

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.city.controller.Controller
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

    val staticScene = StaticScene(this)
    val controller = Controller()

    Frame {

        OnTouch { controller.touch(it) }
        OnKey { controller.key(it) }
        projection = frustum(width = 0.3f * width / height, height = 0.3f, near = 0.3f, far = 500f)
        camera = controller.camera(this)

        controller.update(frameInfo.dt)

        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2f, -5f, 0f).normalize(), white(5.0f)) {
            Cascade(1024, 0.3f, 2.8f, 0f to 60f, pccf())
            Cascade(1024, 2.0f, 25.0f, 0f to 60f, vsm())
            Cascade(1024, 20.0f, 60.0f, 0f to 60f, vsm())
        }
        for (xx in 0..4) {
            for (zz in 0..4) {
                PointLight(Vec3(-192f + 4 + 96f * xx, 8f, -192f + 4 + 96f * zz), white(1.1f))
            }
        }

        staticScene.render(this)
        Scene(gltfResource = "city/swat.glb", transform = controller.character.transform * scale(0.002f))


        Filter(fragment("city/shadow-debug.frag"))

    }
}