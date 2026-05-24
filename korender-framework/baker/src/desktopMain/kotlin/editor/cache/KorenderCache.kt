package com.zakgof.korender.baker.editor.cache

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.scope.FrameScope
import editor.model.entity.EntityModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.mutableMapOf

object KorenderCache {

    private val modelSnapStates = mutableMapOf<String, MutableStateFlow<ImageBitmap?>>()

    fun modelSnap(entityModel: EntityModel) : StateFlow<ImageBitmap?> =
        stateFlowLoader(modelSnapStates, entityModel) {

        }


    private fun <K, V> stateFlowLoader(
        map: MutableMap<String, MutableStateFlow<V?>>,
        key: K,
        korenderBlock: FrameScope.() -> Unit
    ): StateFlow<V?> = map.
}