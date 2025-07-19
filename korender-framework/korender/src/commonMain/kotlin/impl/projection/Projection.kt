package com.zakgof.korender.impl.projection

import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode

internal class Projection (
    override val width: Float,
    override val height: Float,
    override val near: Float,
    override val far: Float,
    val mode: ProjectionMode
) : ProjectionDeclaration

internal object FrustumProjectionMode: ProjectionMode

internal object OrthoProjectionMode: ProjectionMode

internal class LogProjectionMode(c: Float): ProjectionMode