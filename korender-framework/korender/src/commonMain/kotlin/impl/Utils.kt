package com.zakgof.korender.impl

import com.zakgof.korender.resources.Res
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(
    ExperimentalResourceApi::class, DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class
)
fun resourceBytes(resource: String): ByteArray {
    // TODO total rewrite required
    val reader = GlobalScope.async {
        // TODO try catch, propagate exception
        Res.readBytes("files/$resource")
    }
    while (!reader.isCompleted) {
    }

    return reader.getCompleted()
}