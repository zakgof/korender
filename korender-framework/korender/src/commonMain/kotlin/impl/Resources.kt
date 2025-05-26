package com.zakgof.korender.impl

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.gl.GL.glGetError
import com.zakgof.korender.resources.Res
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> Deferred<T>.resultOrNull(): T? = if (this.isCompleted) this.getCompleted() else null

internal suspend fun resourceBytes(appResourceLoader: ResourceLoader, resource: String): ByteArray {
    println("Loading resource $resource")
    if (resource.startsWith("!")) {
        return Res.readBytes("files/" + resource.substring(1))
    }
    return appResourceLoader.invoke("files/$resource")
}

internal fun absolutizeResource(resource: String, referrer: String): String {
    if (resource.startsWith("data:"))
        return resource;
    return referrer.split("/").dropLast(1).joinToString("/") + "/" + resource;
}

internal fun ignoringGlError(block: () -> Unit) {
    block()
    @Suppress("ControlFlowWithEmptyBody")
    while (glGetError() != 0) {
    }
}

internal fun checkGlError(point: String) {
    val error = glGetError()
    if (error != 0) {
        throw KorenderException("GL error $error $point")
    }
}