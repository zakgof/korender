package com.zakgof.korender.glgpu

import com.zakgof.korender.Gpu
import com.zakgof.korender.geometry.Attribute
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.ShaderDebugInfo
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap
import java.nio.ByteBuffer

class GlGpu : Gpu {
    override fun createMesh(
        attrs: List<Attribute>,
        vertexSize: Int,
        isDynamic: Boolean
    ): GpuMesh = GlGpuMesh(attrs, vertexSize, isDynamic)

    override fun createShader(
        title: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader = GlGpuShader(title, vertCode, fragCode, vertDebugInfo, fragDebugInfo)

    // TODO: texture formats
    override fun createTexture(width: Int, height: Int, bytes: ByteBuffer, filter: TextureFilter, wrap: TextureWrap, aniso: Int, alpha: Boolean): GpuTexture =
        GlGpuTexture(width, height, bytes, filter, wrap, aniso, alpha)

    override fun createFrameBuffer(width: Int, height: Int, useDepthBuffer: Boolean) =
        GlGpuFrameBuffer(width, height, useDepthBuffer)
}