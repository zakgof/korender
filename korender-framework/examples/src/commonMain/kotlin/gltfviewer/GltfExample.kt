package com.zakgof.korender.examples.gltfviewer

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.launch

private const val USER_CAMERA = "User Camera"

@Composable
fun GltfExample() = Row {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var models by remember { mutableStateOf(listOf<Model>()) }
    var selectedModel by remember { mutableStateOf<Model?>(null) }
    var cameras by remember { mutableStateOf(listOf<String>(USER_CAMERA)) }
    var selectedCamera by remember { mutableStateOf<String>(USER_CAMERA) }

    LaunchedEffect(null) {
        models = GltfDownloader.list()
        selectedModel = models[0]
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        scrollState.scrollBy(-delta)
                    }
                }
            )
    ) {
        FixedItemsDropdown("Model", models, { it.label }, selectedModel, { selectedModel = it })
        FixedItemsDropdown("Camera", cameras, { it }, selectedCamera, { selectedCamera = it })
    }

    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(20.z, 0.y)
        OnTouch { orbitCamera.touch(it) }
        var currentGltf: String = ""
        Frame {
            DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
            AmbientLight(white(0.6f))
            selectedModel?.let { model ->
                Gltf(
                    resource = model.file,
                    resourceLoader = { GltfDownloader.load(model.folder, model.format, it) },
                    transform = if (selectedCamera == USER_CAMERA) rotate(1.y, frameInfo.time) else Transform.IDENTITY,
                    onUpdate = { update ->
                        cameras = listOf(USER_CAMERA) + update.cameras.mapIndexed { index, cam -> cam.name ?: "Gltf camera $index" }
                        if (!cameras.contains(selectedCamera)) {
                            selectedCamera = cameras.first()
                        }
                        val c = update.cameras.filterIndexed { index, cam -> selectedCamera == (cam.name ?: "Gltf camera $index") }.firstOrNull()
                        if (c != null) {
                            camera = c.camera
                            projection = c.projection
                        } else {
                            if (currentGltf != model.file) {
                                val bs = boundingSphere(update.instances.first().rootNode)
                                camera = cameraFor(bs)
                                projection = projectionFor(bs, width.toFloat() / height)
                                currentGltf = model.file
                            }
                        }
                    }
                )
            }
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
                }
            }
        }
    }
}

