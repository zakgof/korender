package com.zakgof.korender

import com.zakgof.korender.geometry.Attribute
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuMesh
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.ShaderDebugInfo
import com.zakgof.korender.material.TextureFilter
import com.zakgof.korender.material.TextureWrap
import java.nio.ByteBuffer

interface Gpu {
    fun createMesh(attrs: List<Attribute>, vertexSize: Int, isDynamic: Boolean = false): GpuMesh

    fun createShader(
        title: String,
        vertCode: String,
        fragCode: String,
        vertDebugInfo: ShaderDebugInfo,
        fragDebugInfo: ShaderDebugInfo
    ): GpuShader

    fun createTexture(
        width: Int,
        height: Int,
        bytes: ByteBuffer,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024,
        alpha: Boolean = false
    ): GpuTexture

    fun createFrameBuffer(width: Int, height: Int, useDepthBuffer: Boolean): GpuFrameBuffer

}