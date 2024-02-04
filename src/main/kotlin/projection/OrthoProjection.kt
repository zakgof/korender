package com.zakgof.korender.projection

import com.zakgof.korender.math.Mat4

class OrthoProjection(
    val width: Float,
    val height: Float,
    val near: Float,
    val far: Float,
) : Projection {
    override fun mat4(): Mat4 = Mat4(
        1f / width, 0f, 0f, 0f,
        0f, 1f / height, 0f, 0f,
        0f, 0f,  1f / (far - near), near / (far - near),
        0f, 0f, 0f, 1f
    )
}