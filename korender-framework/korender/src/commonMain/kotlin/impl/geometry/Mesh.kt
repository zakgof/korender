package com.zakgof.korender.impl.geometry

import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.math.BoundingBox

internal interface Mesh : AutoCloseable {
    val gpuMesh: GlGpuMesh
    val modelBoundingBox: BoundingBox?
}