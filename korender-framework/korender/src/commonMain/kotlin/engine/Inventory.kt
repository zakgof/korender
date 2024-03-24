package com.zakgof.korender.engine

import com.zakgof.korender.declaration.FrameBufferDeclaration
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.font.Font
import com.zakgof.korender.font.Fonts
import com.zakgof.korender.geometry.Mesh
import com.zakgof.korender.geometry.Geometry
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.gpu.GpuTexture
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.Textures

internal class Inventory(val gpu: Gpu) {

    private val meshes = Registry<MeshDeclaration, Mesh> { Geometry.create(it, gpu) }
    private val shaders = Registry<ShaderDeclaration, GpuShader> { Shaders.create(it, gpu) }
    private val textures = Registry<String, GpuTexture> { Textures.create(it).build(gpu) }
    private val fonts = Registry<String, Font> { Fonts.load(gpu, it) }
    private val fontMeshes = Registry<Any, Geometry.InstancedMesh> { Geometry.font(gpu, 256) }
    private val frameBuffers = Registry<FrameBufferDeclaration, GpuFrameBuffer> { gpu.createFrameBuffer(it.width, it.height, it.withDepth) }

    fun go(block: Inventory.() -> Unit) {
        meshes.begin()
        shaders.begin()
        textures.begin()
        fonts.begin()
        fontMeshes.begin()
        frameBuffers.begin()
        block.invoke(this)
        meshes.end()
        shaders.end()
        textures.end()
        fonts.end()
        fontMeshes.end()
        frameBuffers.end()
    }

    fun mesh(decl: MeshDeclaration): Mesh = meshes[decl]

    fun shader(decl: ShaderDeclaration): GpuShader = shaders[decl]

    fun texture(decl: String): GpuTexture = textures[decl]
    fun hasMesh(decl: MeshDeclaration): Boolean = meshes.has(decl)
    fun font(fontResource: String): Font = fonts[fontResource]
    fun fontMesh(id: Any): Geometry.InstancedMesh = fontMeshes[id]
    fun frameBuffer(decl: FrameBufferDeclaration): GpuFrameBuffer = frameBuffers[decl]


}