package com.zakgof.korender.impl.geometry

import com.zakgof.korender.impl.gpu.GpuMesh
import com.zakgof.korender.math.BoundingBox

interface Mesh : AutoCloseable {
    val gpuMesh: GpuMesh
    val modelBoundingBox: BoundingBox?
}