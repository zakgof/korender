package com.zakgof.korender.impl.prefab

import com.zakgof.korender.Prefab
import com.zakgof.korender.impl.context.DefaultFrameScope

internal interface InternalPrefab<S> : Prefab<S> {
    fun render(fc: DefaultFrameScope, block: S.() -> Unit)
}