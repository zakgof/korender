package com.zakgof.korender.impl

import com.zakgof.korender.resources.Res
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.compose.resources.ExperimentalResourceApi

var preResources = mutableMapOf<String, ByteArray>()

@OptIn(DelicateCoroutinesApi::class, ExperimentalResourceApi::class)
fun preReadResources(vararg resource: String, callback: () -> Unit) {
    val deferred = GlobalScope.async {
        resource.forEach {
            val bytes = Res.readBytes("files/$it")
            preResources[it] = bytes
        }
        callback()
    }
}

@OptIn(
    ExperimentalResourceApi::class, DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class
)
fun resourceBytes(resource: String): ByteArray {

    val pre = preResources[resource]
    if (pre == null)
        println("Missing resource $resource")
    else
        return pre

    return ByteArray(0)

    // TODO total rewrite required
    println("resourceBytes resource $resource")
    val reader = GlobalScope.async {
        // TODO try catch, propagate exception
        try {
            println("resourceBytes async part of resource $resource")
            Res.readBytes("files/$resource")
        } catch (e: Exception) {
            println("Exception while reading resouce $resource: $e ")
            ByteArray(0)
        }
    }
    while (!reader.isCompleted) {
        println("Reading resource loop...")
    }

    return reader.getCompleted()
}