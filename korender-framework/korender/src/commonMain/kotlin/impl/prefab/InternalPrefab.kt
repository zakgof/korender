package com.zakgof.korender.impl.prefab

import com.zakgof.korender.Material
import com.zakgof.korender.Prefab
import com.zakgof.korender.context.FrameContext

interface InternalPrefab<T : Material> : Prefab<T> {
    fun render(fc: FrameContext, material: T)
}