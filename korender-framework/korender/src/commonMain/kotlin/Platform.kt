package com.zakgof.korender

import com.zakgof.korender.scope.KorenderScope
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.image.InternalImage
import kotlinx.coroutines.Deferred

internal expect object Platform {

    val target: KorenderScope.TargetPlatform

    internal fun createImage(width: Int, height: Int, format: PixelFormat): InternalImage

    internal fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage>

    internal fun loadFont(bytes: ByteArray): Deferred<FontDef>

    fun nanoTime(): Long

}