package com.zakgof.korender

import com.zakgof.korender.geometry.Attribute
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.ShaderDebugInfo
import java.nio.ByteBuffer

interface Gpu {
    fun createMesh(
        vb: ByteBuffer,
        ib: ByteBuffer,
        vertices: Int,
        indices: Int,
        attrs: List<Attribute>,
        vertexSize: Int,
        isDynamic: Boolean = false
    ): GpuMesh

    fun createShader(
        title: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader
}