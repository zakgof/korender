package com.zakgof.korender.projection

import com.zakgof.korender.math.Mat4

class FrustumProjection(
    val width: Float,
    val height: Float,
    val near: Float,
    val far: Float
) : Projection {

    override fun mat4(): Mat4 {
        return Mat4(
            1.0f * near / width, 0f, 0f, 0f,
            0f, 1.0f * near / height, 0f, 0f,
            0f, 0f, (far + near) / (far - near), -2.0f * far * near / (far - near),
            0f, 0f, -1f, 0f
        )
    }
}
