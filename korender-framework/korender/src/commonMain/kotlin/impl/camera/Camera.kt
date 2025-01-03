package com.zakgof.korender.impl.camera

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

internal interface Camera: CameraDeclaration {
    val mat4: Mat4
}
