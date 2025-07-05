package com.zakgof.korender.impl.camera

import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

internal class DefaultCamera(override val position: Vec3, override val direction: Vec3, override val up: Vec3) : Camera {

    override val mat4: Mat4

    init {
        val right = direction % up
        mat4 = Mat4(
            right.x, right.y, right.z, -position * right,
            up.x, up.y, up.z, -position * up,
            -direction.x, -direction.y, -direction.z, position * direction,
            0f, 0f, 0f, 1f
        )
    }

}
