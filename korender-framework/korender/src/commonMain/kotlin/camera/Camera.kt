package com.zakgof.korender.camera

import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

interface Camera {
    val position: Vec3
    val mat4: Mat4
}
