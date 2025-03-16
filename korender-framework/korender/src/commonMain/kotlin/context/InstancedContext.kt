package com.zakgof.korender.context

import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

interface InstancingDeclaration {
}

interface InstancedBillboardsContext {
    fun Instance(pos: Vec3, scale: Vec2 = Vec2.ZERO, rotation: Float = 0f)
}

interface InstancedRenderablesContext {
    fun Instance(transform: Transform)
}