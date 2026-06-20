package com.zakgof.korender

import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.scope.KorenderScope
import kotlinx.coroutines.Deferred

internal expect object Platform {

    val target: KorenderScope.TargetPlatform

    internal fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage>

    internal fun loadFont(bytes: ByteArray): Deferred<FontDef>

    fun nanoTime(): Long

}