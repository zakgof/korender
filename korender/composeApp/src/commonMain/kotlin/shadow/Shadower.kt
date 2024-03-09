package com.zakgof.korender.shadow

import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

interface Shadower {
    val texture: GpuTexture
    var camera: Camera?
    var projection: Projection?
    fun add(renderable: Renderable): Boolean
    fun render(light: Vec3)
}