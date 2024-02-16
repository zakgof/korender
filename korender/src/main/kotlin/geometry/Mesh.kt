package com.zakgof.korender.geometry

import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.math.BoundingBox

interface Mesh {
    val gpuMesh : GpuMesh

    val modelBoundingBox: BoundingBox
}