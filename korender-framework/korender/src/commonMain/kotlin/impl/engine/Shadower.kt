package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

interface Shadower {
    fun render(projection: Projection, camera: Camera, light: Vec3): UniformSupplier
}