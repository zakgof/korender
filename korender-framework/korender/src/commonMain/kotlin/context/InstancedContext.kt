package com.zakgof.korender.context

import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

interface InstancingDeclaration

interface GltfInstancingDeclaration

interface BillboardInstancingDeclaration

interface InstancedBillboardsContext {
    fun Instance(pos: Vec3, scale: Vec2 = Vec2(1f, 1f), rotation: Float = 0f)
}

interface InstancedRenderablesContext {
    fun Instance(transform: Transform)
}

interface InstancedGltfContext {
    fun Instance(transform: Transform, time: Float? = null, animation: Int? = null)
}