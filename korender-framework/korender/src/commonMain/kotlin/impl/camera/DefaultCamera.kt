package com.zakgof.korender.impl.camera

import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

internal class DefaultCamera(
    override val position: Vec3,
    override val direction: Vec3,
    override val up: Vec3,
) : Camera {

    override val mat4: Mat4 = buildMatrix(position, direction, up)

    companion object {
        private fun buildMatrix(pos: Vec3, dir: Vec3, up: Vec3): Mat4 {
            val right = dir % up
            return Mat4(
                right.x, right.y, right.z, -pos * right,
                up.x, up.y, up.z, -pos * up,
                -dir.x, -dir.y, -dir.z, pos * dir,
                0f, 0f, 0f, 1f
            )
        }
    }

    constructor(m: Mat4) : this(
        direction = Vec3(-m.m20, -m.m21, -m.m22),
        up = Vec3(m.m10, m.m11, m.m12),
        position = -Vec3(
            m.m00 * (-m.m03) + m.m10 * (-m.m13) + (-m.m20) * (m.m23),
            m.m01 * (-m.m03) + m.m11 * (-m.m13) + (-m.m21) * (m.m23),
            m.m02 * (-m.m03) + m.m12 * (-m.m13) + (-m.m22) * (m.m23)
        )
    )
}
