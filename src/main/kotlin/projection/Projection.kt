package com.zakgof.korender.projection

import com.zakgof.korender.math.Mat4

interface Projection {
    fun mat4(): Mat4
}