package editor.walk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.editor.walk.Controller
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.impl.scene.KrModel
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Vec3
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun walkerDialog(): (Pair<KrModel, ByteArray>) -> Unit {

    var show by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<Pair<KrModel, ByteArray>?>(null) }
    val openDialog = { data1: Pair<KrModel, ByteArray> ->
        show = true
        data = data1
    }

    if (show) {
        DialogWindow(
            title = "Dry Run",
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(800.dp, 600.dp))
        ) {
            Korender({
                if (it == "scene/foobar.kr") Cbor.encodeToByteArray(data!!.first) else Res.readBytes("files/$it")
            }) {
                val controller = Controller(data!!.second)
                OnKey { controller.key(it) }
                OnTouch { controller.touch(it) }
                val sky = fastCloudSky()
                Frame {
                    DeferredShading {
                        Shading {
                            env = sky
                        }
                        Hbao(
                            downsample = 2,
                            sampleCount = 12,
                            radius = 3.0f,
                            bias = 0.1f,
                            intensity = 1.4f,
                            blurRadius = 16f
                        )
                    }
                    Sky(sky)
                    PostProcess(fxaa())
                    DirectionalLight(Vec3(1f, -2f, -1f), white(2f)) {
                        Cascade(1024, 0.5f, 35f, -0.2f to 20f, hardwarePcf(samples = 8, blurRadius = 1.5f, bias = 0.01f))
                        Cascade(512, 30f, 100f, -0.2f to 20f, hardwarePcf(samples = 6, blurRadius = 1.5f, bias = 0.01f))
                    }
                    DirectionalLight(Vec3(-1f, -2f, 1f), white(0.5f))
                    controller.update(frameInfo.dt, frameInfo.time)
                    projection = projection(0.5f * width / height, 0.5f, 0.5f, 1000f)
                    camera = controller.camera()
                    Model("scene/foobar.kr") {}
                    Model(resource = "walk/swat-woman.glb", transform = controller.player())
                    Gui {
                        Column {
                            Filler()
                            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                        }
                    }
                    // PostProcess(customPostProcessingFilter("!shader/effect/shadow-debug.frag"))
                }
            }
        }
    }
    return openDialog
}