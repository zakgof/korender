package com.zakgof.korender.projection

import com.zakgof.korender.math.Mat4

class FrustumProjection(
    val width: Float,
    val height: Float,
    val near: Float,
    val far: Float
) : Projection {

    override val mat4 = Mat4(
        2.0f * near / width, 0f, 0f, 0f,
        0f, 2.0f * near / height, 0f, 0f,
        0f, 0f, -far / (far - near), -far * near / (far - near),
        0f, 0f, -1f, 0f
    )
}
