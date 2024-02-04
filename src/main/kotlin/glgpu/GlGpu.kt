package com.zakgof.korender.glgpu

import com.zakgof.korender.Attribute
import com.zakgof.korender.Gpu
import com.zakgof.korender.material.ShaderDebugInfo
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import java.nio.ByteBuffer

class GlGpu : Gpu {
    override fun createMesh(
        vb: ByteBuffer,
        ib: ByteBuffer,
        vertices: Int,
        indices: Int,
        attrs: List<Attribute>,
        vertexSize: Int,
        isDynamic: Boolean
    ): GpuMesh = GlGpuMesh(vb, ib, vertices, indices, attrs, vertexSize, isDynamic)

    override fun createShader(
        title: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader = GlGpuShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)
}