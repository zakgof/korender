package com.zakgof.korender.impl.engine

import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.impl.font.Font
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.Mesh
import com.zakgof.korender.impl.gpu.Gpu
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import com.zakgof.korender.impl.gpu.GpuShader
import com.zakgof.korender.impl.gpu.GpuTexture
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.impl.material.Texturing

internal class Inventory(val gpu: Gpu) {

    private val meshes = Registry<MeshDeclaration, Mesh> { Geometry.create(it, gpu) }
    private val shaders = Registry<ShaderDeclaration, GpuShader> { Shaders.create(it, gpu) }
    private val textures = Registry<TextureDeclaration, GpuTexture> { Texturing.create(it, gpu) }
    private val fonts = Registry<String, Font> { Fonts.load(gpu, it) }
    private val fontMeshes = Registry<Any, Geometry.MultiMesh> { Geometry.font(gpu, 256) }
    private val frameBuffers = Registry<FrameBufferDeclaration, GpuFrameBuffer> { gpu.createFrameBuffer(it.id, it.width, it.height, it.withDepth) }

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

    fun texture(decl: TextureDeclaration): GpuTexture = textures[decl]
    fun hasMesh(decl: MeshDeclaration): Boolean = meshes.has(decl)
    fun font(fontResource: String): Font = fonts[fontResource]
    fun fontMesh(id: Any): Geometry.MultiMesh = fontMeshes[id]
    fun frameBuffer(decl: FrameBufferDeclaration): GpuFrameBuffer = frameBuffers[decl]


}