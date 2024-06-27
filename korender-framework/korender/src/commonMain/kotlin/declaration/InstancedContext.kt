package com.zakgof.korender.declaration

import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

interface InstancedBillboardsContext {
    fun Instance(pos: Vec3, scale: Vec2 = Vec2.ZERO, phi: Float = 0f)
}

interface InstancedRenderablesContext {
    fun Instance(transform: Transform)
}