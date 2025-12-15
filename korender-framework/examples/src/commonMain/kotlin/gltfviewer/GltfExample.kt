package com.zakgof.korender.examples.gltfviewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zakgof.app.resources.Res
import com.zakgof.korender.CubeTextureSide
import com.zakgof.korender.Korender
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.gltf.GltfUpdate
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

private const val AUTO_CAMERA = "Auto Camera"

@Composable
fun GltfLibraryExample() = Row {
    var models by remember { mutableStateOf(listOf<Model>()) }
    var selectedModel by remember { mutableStateOf<Model?>(null) }
    var cameras by remember { mutableStateOf(listOf<String>(AUTO_CAMERA)) }
    var selectedCamera by remember { mutableStateOf<String>(AUTO_CAMERA) }
    var currentGltf by remember { mutableStateOf("") }
    var bs by remember { mutableStateOf(BoundingSphere(Vec3.ZERO, 1f)) }

    LaunchedEffect(null) {
        models = GltfDownloader.list()
        selectedModel = models.find { it.folder == "Avocado" }
    }

    fun KorenderContext.updateCamera(update: GltfUpdate) {
        cameras = listOf(AUTO_CAMERA) + update.cameras.mapIndexed { index, cam -> cam.name ?: "Gltf camera $index" }
        if (!cameras.contains(selectedCamera)) {
            selectedCamera = cameras.first()
        }
        val c = update.cameras.filterIndexed { index, cam -> selectedCamera == (cam.name ?: "Gltf camera $index") }.firstOrNull()
        if (c != null) {
            camera = c.camera
            projection = c.projection
        } else {
            if (currentGltf != selectedModel?.file) {
                bs = boundingSphere(update.instances.first().rootNode)
                currentGltf = selectedModel!!.file
            }
            camera = cameraFor(bs)
            projection = projectionFor(bs, width.toFloat() / height)
        }
    }

    Box(modifier = Modifier.weight(1f)) {
        Korender(appResourceLoader = { Res.readBytes(it) }) {
            val env = cubeTexture(CubeTextureSide.entries.associateWith { "cube/sea/${it.toString().lowercase()}.jpg" })
            Frame {
                DeferredShading {
                    Shading(ibl(env))
                }
                OnLoading {
                    Gui {
                        Column {
                            Filler()
                            Text(id = "loading", text = "Loading ${selectedModel?.folder ?: "metadata"}...", height = 40, color = ColorRGBA(0x66FF55A0))
                        }
                    }
                }
                DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(1f))
                AmbientLight(white(0.1f))
                selectedModel?.let { model ->
                    Gltf(
                        ibl(env),
                        resource = model.file,
                        resourceLoader = { GltfDownloader.load(model.folder, model.format, it) },
                        transform = if (selectedCamera == AUTO_CAMERA) rotate(bs.center, 1.y, frameInfo.time * 0.3f) else Transform.IDENTITY,
                        onUpdate = { update -> updateCamera(update) }
                    )
                }
                Sky(ibl(env))
                Gui {
                    Column {
                        Filler()
                        Text(id = "fps", text = "${selectedModel?.folder ?: ""}: FPS ${frameInfo.avgFps.toInt()}", height = 40, color = ColorRGBA(0x66FF55A0))
                    }
                }
            }
        }
    }

    Column {
        FixedItemsDropdown("Model", models, { it.label }, selectedModel, { selectedModel = it })
        FixedItemsDropdown("Camera", cameras, { it }, selectedCamera, { selectedCamera = it })
    }
}

