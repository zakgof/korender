package com.zakgof.korender.impl.projection

import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.Mat4

internal interface Projection : ProjectionDeclaration {
    val mat4: Mat4
}