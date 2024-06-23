package com.zakgof.korender.declaration

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

interface FrameContext : PassContext {
    fun Shadow(block: ShadowContext.() -> Unit)
    fun Pass(block: PassContext.() -> Unit)
}