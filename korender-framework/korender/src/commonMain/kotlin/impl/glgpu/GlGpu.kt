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
        attrs: List<Attribute>,
        vertexSize: Int,
        isDynamic: Boolean,
        isLongIndex: Boolean
    ): GpuMesh = GlGpuMesh(attrs, vertexSize, isDynamic, isLongIndex)

    override fun createShader(
        title: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader = GlGpuShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)

    // TODO: more texture formats
    override fun createTexture(
        width: Int,
        height: Int,
        bytes: ByteBuffer,
        filter: TextureFilter,
        wrap: TextureWrap,
        aniso: Int,
        format: GpuTexture.Format
    ): GpuTexture =
        GlGpuTexture(width, height, bytes, filter, wrap, aniso, format)

    override fun createFrameBuffer(width: Int, height: Int, useDepthBuffer: Boolean) =
        GlGpuFrameBuffer(width, height, useDepthBuffer)
}