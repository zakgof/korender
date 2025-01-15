package com.zakgof.korender.impl.projection

import com.zakgof.korender.OrthoProjectionDeclaration
import com.zakgof.korender.math.Mat4

internal class OrthoProjection(
    override val width: Float,
    override val height: Float,
    override val near: Float,
    override val far: Float,
) : OrthoProjectionDeclaration, Projection {
    override val mat4 = Mat4(
        1f / width, 0f, 0f, 0f,
        0f, 1f / height, 0f, 0f,
        0f, 0f, -2f / (far - near), -(far + near) / (far - near),
        0f, 0f, 0f, 1f
    )
}