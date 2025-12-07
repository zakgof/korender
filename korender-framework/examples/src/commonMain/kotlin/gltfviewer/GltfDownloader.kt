package com.zakgof.korender.examples.gltfviewer

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType.Text.Plain
import io.ktor.serialization.kotlinx.json.json
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


object GltfDownloader {

    suspend fun list(): List<Model> {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true },
                    contentType = Plain
                )
            }
        }
        return client.get("https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/refs/heads/main/Models/model-index.json").body()
    }

    suspend fun load(folder: String, format: String, file: String): ByteArray {
        val fileFix = if (file.startsWith("/")) file.substring(1) else file
        val url = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/refs/heads/main/Models/${folder}/${format}/${fileFix}"
        val bytes = HttpClient().get(url).readRawBytes()
        println("Loaded ${bytes.size} bytes from model $url")
        return bytes
    }
}

