package city

import androidx.compose.runtime.Composable
import city.controller.Controller
import com.zakgof.insecto.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.White
import com.zakgof.korender.math.Color.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random


fun Int.chance(block: () -> Unit) {
    if (Random.nextInt(100) < this) block()
}

@Composable
@OptIn(ExperimentalResourceApi::class)
fun App() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val staticScene = StaticScene(this)
    val controller = Controller()

    // val orbitCamera = OrbitCamera(this, -103f.z + 0.3f.y + 3.2f.x, -102.z + 0.3f.y + 3.2f.x)


    Frame {

        OnTouch { controller.touch(it) }
        OnKey { controller.key(it) }
        projection = frustum(width = 0.3f * width / height, height = 0.3f, near = 0.3f, far = 500f)
        camera = controller.character.camera(projection, width, height)

        controller.update(frameInfo.dt)

        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2f, -5f, 0f).normalize(), white(5.0f)) {
            Cascade(1024, 0.3f, 2.2f, 30f)
            Cascade(1024, 2.0f, 50.0f, 98f)
        }
        for (xx in 0..4) {
            for (zz in 0..4) {
                PointLight(Vec3(-192f + 4 + 96f * xx, 8f, -192f + 4 + 96f * zz), white(1.1f))
            }
        }

        staticScene.render(this)

        Scene(gltfResource = "city/swat.glb", transform = scale(0.002f).translate(-102.z + 3.2f.x))

    }
}