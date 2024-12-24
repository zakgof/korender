package com.zakgof.korender.impl.engine

import com.zakgof.korender.AsyncContext
import com.zakgof.korender.impl.font.Font
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.Mesh
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.gltf.GltfLoaded
import com.zakgof.korender.impl.gltf.GltfScenes
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.impl.material.Texturing
import com.zakgof.korender.material.TextureDeclaration
import com.zakgof.korender.mesh.MeshDeclaration

internal class Inventory(asyncContext: AsyncContext) {

    private val meshes = Registry<MeshDeclaration, Mesh>(asyncContext) { Geometry.create(it, asyncContext.appResourceLoader) }
    private val shaders = Registry<ShaderDeclaration, GlGpuShader>(asyncContext) { Shaders.create(it, asyncContext.appResourceLoader) }
    private val textures = Registry<TextureDeclaration, GlGpuTexture>(asyncContext) { Texturing.create(it, asyncContext.appResourceLoader) }
    private val fonts = Registry<String, Font>(asyncContext) { Fonts.load(it, asyncContext.appResourceLoader) }
    private val fontMeshes = Registry<Any, Geometry.MultiMesh>(asyncContext) { Geometry.font(256) }
    private val frameBuffers = Registry<FrameBufferDeclaration, GlGpuFrameBuffer>(asyncContext) { GlGpuFrameBuffer(it.id, it.width, it.height, it.withDepth) }
    private val gltfs = Registry<GltfDeclaration, GltfLoaded>(asyncContext) { GltfScenes.load(it, asyncContext.appResourceLoader)}

    fun go(block: Inventory.() -> Unit) {
        meshes.begin()
        shaders.begin()
        textures.begin()
        fonts.begin()
        fontMeshes.begin()
        frameBuffers.begin()
        gltfs.begin()
        block.invoke(this)
        meshes.end()
        shaders.end()
        textures.end()
        fonts.end()
        fontMeshes.end()
        frameBuffers.end()
        gltfs.end()
    }

    fun mesh(decl: MeshDeclaration): Mesh? = meshes[decl]
    fun shader(decl: ShaderDeclaration): GlGpuShader? = shaders[decl]
    fun texture(decl: TextureDeclaration): GlGpuTexture? = textures[decl]
    fun font(fontResource: String): Font? = fonts[fontResource]
    fun fontMesh(id: Any): Geometry.MultiMesh? = fontMeshes[id]
    fun frameBuffer(decl: FrameBufferDeclaration): GlGpuFrameBuffer? = frameBuffers[decl]
    fun gltf(decl: GltfDeclaration): GltfLoaded? = gltfs[decl]
}