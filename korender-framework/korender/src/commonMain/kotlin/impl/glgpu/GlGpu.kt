package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.declaration.TextureFilter
import com.zakgof.korender.declaration.TextureWrap
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuMesh
import com.zakgof.korender.impl.gpu.GpuShader
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.ShaderDebugInfo
import java.nio.ByteBuffer

class GlGpu : Gpu {
    override fun createMesh(
        name: String,
        attrs: List<Attribute>,
        vertexSize: Int,
        isDynamic: Boolean,
        isLongIndex: Boolean
    ): GpuMesh = GlGpuMesh(name, attrs, vertexSize, isDynamic, isLongIndex)

    override fun createShader(
        name: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader = GlGpuShader(name, vertCode, fragCode, vertDebugInfo, fragDebugInfo)

    // TODO: more texture formats
    override fun createTexture(
        name: String,
        width: Int,
        height: Int,
        bytes: ByteBuffer,
        filter: TextureFilter,
        wrap: TextureWrap,
        aniso: Int,
        format: GpuTexture.Format
    ): GpuTexture =
        GlGpuTexture(name, width, height, bytes, filter, wrap, aniso, format)

    override fun createFrameBuffer(name: String, width: Int, height: Int, useDepthBuffer: Boolean) =
        GlGpuFrameBuffer(name, width, height, useDepthBuffer)
}