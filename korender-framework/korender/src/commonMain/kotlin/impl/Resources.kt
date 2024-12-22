package com.zakgof.korender.impl

import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.resources.Res
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> Deferred<T>.resultOrNull() : T? = if (this.isCompleted) this.getCompleted() else null

@OptIn(ExperimentalResourceApi::class)
internal suspend fun resourceBytes(appResourceLoader: ResourceLoader, resource: String): ByteArray {
    println("Loading resource $resource")
    if (resource.contains("!")) {
        return appResourceLoader.invoke("files/" + resource.replace("!", ""))
    }
    return Res.readBytes("files/$resource")
}