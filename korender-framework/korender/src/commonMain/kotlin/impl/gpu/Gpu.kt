package com.zakgof.korender.impl.gpu

import com.zakgof.korender.declaration.TextureFilter
import com.zakgof.korender.declaration.TextureWrap
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.impl.material.ShaderDebugInfo
import java.nio.ByteBuffer

interface Gpu {
    fun createMesh(attrs: List<Attribute>, vertexSize: Int, isDynamic: Boolean, isLongIndex: Boolean): GpuMesh

    fun createShader(
        title: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader

    fun createFrameBuffer(width: Int, height: Int, useDepthBuffer: Boolean): GpuFrameBuffer

    fun createTexture(
        width: Int,
        height: Int,
        bytes: ByteBuffer,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024,
        format: GpuTexture.Format = GpuTexture.Format.RGB
    ): GpuTexture
}