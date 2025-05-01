package com.zakgof.korender

import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.image.InternalImage
import kotlinx.coroutines.Deferred

internal expect object Platform {

    val target: KorenderContext.TargetPlatform

    internal fun createImage(width: Int, height: Int, format: Image.Format): Image

    internal fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage>

    internal fun loadFont(bytes: ByteArray): Deferred<FontDef>

    fun nanoTime(): Long

}

internal interface AsyncContext {
    val appResourceLoader: ResourceLoader
    fun <R> call(function: suspend () -> R): Deferred<R>
}

