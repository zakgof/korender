package com.zakgof.korender.camera

import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

class DefaultCamera(val pos: Vec3, val dir: Vec3, val up: Vec3) : Camera {

    override fun mat4(): Mat4 {
        val right = dir % up
        return Mat4(
            right.x, right.y, right.z, -pos * right,
            up.x, up.y, up.z, -pos * up,
            -dir.x, -dir.y, -dir.z, pos * dir,
            0f, 0f, 0f, 1f
        )
    }

    override fun position() = pos
}
