package com.zakgof.korender.impl.prefab

import com.zakgof.korender.Material
import com.zakgof.korender.Prefab
import com.zakgof.korender.impl.context.DefaultFrameScope

internal interface InternalPrefab<T : Material> : Prefab<T> {
    fun render(fc: DefaultFrameScope, material: T)
}