package com.zakgof.korender.examples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.camera.OrbitCamera
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType.Text.Plain
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ModelSerializer::class)
class Model(val label: String, val folder: String, val format: String, val file: String)


object ModelSerializer : KSerializer<Model> {

    override val descriptor = buildClassSerialDescriptor("Model") {
        element<String>("label")
        element<String>("folder")
        element<String>("format")
        element<String>("file")
    }

    override fun deserialize(decoder: Decoder): Model {
        val obj = (decoder as JsonDecoder).decodeJsonElement().jsonObject
        val label = obj["label"]!!.jsonPrimitive.content
        val folder = obj["name"]!!.jsonPrimitive.content
        val gltfBinary = obj["variants"]?.jsonObject["glTF-Binary"]?.jsonPrimitive?.content
        val format = if (gltfBinary != null) "glTF-Binary" else "glTF"
        val file = gltfBinary ?: obj["variants"]?.jsonObject["glTF"]!!.jsonPrimitive.content
        return Model(label, folder, format, file)
    }

    override fun serialize(encoder: Encoder, value: Model) = error("Not implemented")
}


@Composable
fun GltfExample() = Row {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var models by remember { mutableStateOf(listOf<Model>()) }
    var gltfModelName by remember { mutableStateOf<String?>(null) }
    var gltfBytes by remember { mutableStateOf(ByteArray(0)) }

    LaunchedEffect(null) {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true },
                    contentType = Plain
                )
            }
        }
        models = client.get("https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/refs/heads/main/Models/model-index.json").body()

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
        models.forEach { model ->
            Text(model.label,
                fontSize = 8.sp,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        val client = HttpClient()
                        gltfBytes = client.get("https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/refs/heads/main/Models/${model.folder}/${model.format}/${model.file}").readRawBytes()
                        gltfModelName = model.file
                        println("Loaded ${gltfBytes.size} bytes from model ${model.format}/${model.file}")
                    }
                }
            )
        }
    }

    Korender(appResourceLoader = { Res.readBytes(it) }) {
        val orbitCamera = OrbitCamera(20.z, 0.y)
        OnTouch { orbitCamera.touch(it) }
        Frame {
            camera = orbitCamera.run { camera() }
            DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
            AmbientLight(white(0.6f))
            gltfModelName?.let {
                Gltf(id = it, bytes = gltfBytes, transform = scale(100f).rotate(1.y, frameInfo.time))
            }
        }
    }
}