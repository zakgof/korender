package com.zakgof.korender.impl.projection

import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.math.Mat4

internal class FrustumProjection(
    override val width: Float,
    override val height: Float,
    override val near: Float,
    override val far: Float
) : FrustumProjectionDeclaration, Projection {

    override val mat4 = Mat4(
        2.0f * near / width, 0f, 0f, 0f,
        0f, 2.0f * near / height, 0f, 0f,
        0f, 0f, -(far + near) / (far - near), -2.0f * far * near / (far - near),
        0f, 0f, -1f, 0f
    )
}
