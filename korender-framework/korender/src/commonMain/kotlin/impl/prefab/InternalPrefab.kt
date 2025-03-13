package com.zakgof.korender.impl.prefab

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.Prefab
import com.zakgof.korender.context.FrameContext

interface InternalPrefab : Prefab {
    fun render(fc: FrameContext, vararg materialModifiers: MaterialModifier)
}