package com.zakgof.korender.baker.editor.walk

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
import com.zakgof.korender.Prefab
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Vec3
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun dryRunDialog(): (SceneModel) -> Unit {

    var show by remember { mutableStateOf(false) }
    var scene by remember { mutableStateOf<SceneModel?>(null) }
    val openDialog = { it: SceneModel ->
        show = true
        scene = it
    }

    if (show) {
        DialogWindow(
            title = "Dry Run",
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(800.dp, 600.dp))
        ) {
            Korender({
                if (it.startsWith("files/scene/")) Cbor.encodeToByteArray(scene!!) else Res.readBytes(it)
            }) {
                val controller = Controller()
                val prefab: Prefab = scene("scene/foobar")
                OnKey { controller.key(it) }
                Frame {
                    AmbientLight(white(0.3f))
                    DirectionalLight(Vec3(1f, -2f, 0f), white(3f)) {
                        Cascade(1024, 0.1f, 5f, -0.2f to 4f, hardwarePcf(0.01f))
                    }
                    controller.update(frameInfo.dt, frameInfo.time)
                    projection = projection(0.2f * width / height, 0.2f, 0.2f, 1000f)
                    camera = controller.camera()
                    Renderable(prefab = prefab)
                    Gltf(resource = "walk/swat-woman.glb", transform = controller.player())
                    Gui {
                        Column {
                            Filler()
                            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                        }
                    }
                    PostProcess(fragment("!shader/effect/shadow-debug.frag"))
                }
            }
        }
    }
    return openDialog
}