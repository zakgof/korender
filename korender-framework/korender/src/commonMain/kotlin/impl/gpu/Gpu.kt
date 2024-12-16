package com.zakgof.korender.impl.gpu

import com.zakgof.korender.buffer.Byter
import com.zakgof.korender.impl.geometry.Attribute
import com.zakgof.korender.impl.material.ShaderDebugInfo
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap

interface Gpu {
    fun createMesh(name: String, attrs: List<Attribute>, vertexSize: Int, isDynamic: Boolean, isLongIndex: Boolean): GpuMesh

    fun createShader(
        name: String, vertCode: String, fragCode: String, vertDebugInfo: ShaderDebugInfo, fragDebugInfo: ShaderDebugInfo
    ): GpuShader

    fun createFrameBuffer(name: String, width: Int, height: Int, useDepthBuffer: Boolean): GpuFrameBuffer

    fun createTexture(
        name: String, width: Int, height: Int, bytes: Byter, filter: TextureFilter = TextureFilter.MipMapLinearLinear, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024, format: GpuTexture.Format = GpuTexture.Format.RGB
    ): GpuTexture
}