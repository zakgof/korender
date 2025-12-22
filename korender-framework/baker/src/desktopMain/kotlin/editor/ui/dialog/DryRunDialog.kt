package com.zakgof.korender.baker.editor.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import editor.state.StateHolder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun dryRunDialog(holder: StateHolder): () -> Unit {

    val state by holder.state.collectAsState()
    var show by remember { mutableStateOf(false) }
    val openDialog = { show = true }

    if (show) {
        DialogWindow(
            title = "Dry Run",
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(800.dp, 600.dp))
        ) {
            Korender({
                Cbor.encodeToByteArray(state.lastCompiledSceneModel!!)
            }) {
                val prefab: Prefab = scene("foobar")
                camera = camera(state.camera.position, state.camera.direction, state.camera.up)
                Frame {
                    Renderable(prefab = prefab)
                    Gui {
                        Column {
                            Filler()
                            Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                        }
                    }
                }
            }
        }
    }
    return openDialog
}