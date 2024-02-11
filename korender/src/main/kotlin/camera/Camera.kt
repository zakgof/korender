package com.zakgof.korender.camera

import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

interface Camera {

    fun mat4(): Mat4
    fun position(): Vec3
}
