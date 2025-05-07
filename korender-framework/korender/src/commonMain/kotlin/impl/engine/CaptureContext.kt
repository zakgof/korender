package com.zakgof.korender.impl.engine

import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.math.Vec3

internal class CaptureContext(
    val resolution: Int,
    val position: Vec3,
    val near: Float,
    val far: Float,
    val insideOut: Boolean,
    val defs: Set<String>,
    val sceneDeclaration: SceneDeclaration
)