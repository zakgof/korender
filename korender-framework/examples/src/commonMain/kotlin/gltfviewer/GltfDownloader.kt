package com.zakgof.korender.examples.gltfviewer

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import kotlin.concurrent.Volatile

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


object GltfDownloader {

    private val loadMutex = Mutex()

    @Volatile
    private var activeLoads = 0

    val isLoading: Boolean
        get() = activeLoads > 0

    private suspend inline fun <T> trackLoading(
        crossinline block: suspend () -> T
    ): T {
        loadMutex.withLock {
            activeLoads++
        }

        try {
            return block()
        } finally {
            loadMutex.withLock {
                activeLoads--
            }
        }
    }

    suspend fun list(): List<Model> =
        trackLoading {
            val client = HttpClient {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(
                        Json { ignoreUnknownKeys = true },
                        contentType = ContentType.Text.Plain
                    )
                }
            }
            client
                .get("https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/refs/heads/main/Models/model-index.json")
                .body()
        }

    suspend fun load(
        folder: String,
        format: String,
        file: String
    ): ByteArray =
        trackLoading {
            val fileFix =
                if (file.startsWith("/")) file.substring(1) else file
            val url =
                "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/refs/heads/main/Models/$folder/$format/$fileFix"
                    .decodeURLPart()
                    .encodeURLPath()
            val bytes = HttpClient().get(url).readRawBytes()
            println("Loaded ${bytes.size} bytes from model $url")
            bytes
        }
}