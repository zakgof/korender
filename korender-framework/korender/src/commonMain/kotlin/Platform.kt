package com.zakgof.korender

import com.zakgof.korender.image.Image
import com.zakgof.korender.impl.font.FontDef
import kotlinx.coroutines.Deferred

expect object Platform {

    val name: String

    fun loadImage(bytes: ByteArray, type: String): Deferred<Image>

    fun loadFont(bytes: ByteArray): Deferred<FontDef>

    fun nanoTime(): Long
}

internal interface AsyncContext {
    val appResourceLoader: ResourceLoader
    fun <R> call(function: suspend () -> R): Deferred<R>
}

