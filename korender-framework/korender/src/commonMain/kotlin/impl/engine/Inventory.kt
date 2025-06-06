package com.zakgof.korender.impl.engine

import com.zakgof.korender.AsyncContext
import com.zakgof.korender.impl.font.Font
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.font.InternalFontDeclaration
import com.zakgof.korender.impl.font.InternalFontMeshDeclaration
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.MeshLink
import com.zakgof.korender.impl.glgpu.GlGpuCubeFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.gltf.GltfLoaded
import com.zakgof.korender.impl.gltf.GltfLoader
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.impl.material.Texturing

internal class Inventory(asyncContext: AsyncContext) {

    private val zeroTex = GlGpuTexture.zeroTex()

    private val meshes = Registry<InternalMeshDeclaration, MeshLink>(asyncContext) { Geometry.create(it, asyncContext.appResourceLoader) }
    private val shaders = Registry<ShaderDeclaration, GlGpuShader>(asyncContext) { Shaders.create(it, asyncContext.appResourceLoader, zeroTex) }
    private val textures = Registry<InternalTexture, GlGpuTexture>(asyncContext) { it.generateGpuTexture(asyncContext.appResourceLoader) }
    private val resourceCubeTextures = Registry<ResourceCubeTextureDeclaration, GlGpuCubeTexture>(asyncContext) { Texturing.cube(it, asyncContext.appResourceLoader) }
    private val imageCubeTextures = Registry<ImageCubeTextureDeclaration, GlGpuCubeTexture>(asyncContext) { Texturing.cube(it) }
    private val fonts = Registry<InternalFontDeclaration, Font>(asyncContext) { Fonts.load(it.resource, asyncContext.appResourceLoader) }
    private val fontMeshes = Registry<InternalFontMeshDeclaration, MeshLink>(asyncContext) { Geometry.font(256) }
    private val frameBuffers = Registry<FrameBufferDeclaration, GlGpuFrameBuffer>(asyncContext) { GlGpuFrameBuffer(it.id, it.width, it.height, it.colorTexturePresets, it.withDepth) }
    private val cubeFrameBuffers = Registry<CubeFrameBufferDeclaration, GlGpuCubeFrameBuffer>(asyncContext) { GlGpuCubeFrameBuffer(it.id, it.width, it.height, it.withDepth) }
    private val gltfs = Registry<GltfDeclaration, GltfLoaded>(asyncContext) { GltfLoader.load(it, asyncContext.appResourceLoader) }

    private val registries = listOf(meshes, shaders, textures, fonts, fontMeshes, frameBuffers, cubeFrameBuffers, gltfs)

    fun go(time: Float, generation: Int, block: Inventory.() -> Boolean) {
        registries.forEach { it.begin() }
        val ok = block.invoke(this)
        if (ok) {
            registries.forEach { it.end(time, generation) }
        }
    }

    fun pending() = registries.sumOf { it.pending() }

    fun mesh(decl: InternalMeshDeclaration): MeshLink? = meshes[decl]
    fun shader(decl: ShaderDeclaration): GlGpuShader? = shaders[decl]
    fun texture(decl: InternalTexture): GlGpuTexture? = textures[decl]
    fun cubeTexture(decl: ResourceCubeTextureDeclaration): GlGpuCubeTexture? = resourceCubeTextures[decl]
    fun cubeTexture(decl: ImageCubeTextureDeclaration): GlGpuCubeTexture? = imageCubeTextures[decl]
    fun font(decl: InternalFontDeclaration): Font? = fonts[decl]
    fun fontMesh(decl: InternalFontMeshDeclaration): MeshLink? = fontMeshes[decl]
    fun frameBuffer(decl: FrameBufferDeclaration): GlGpuFrameBuffer? = frameBuffers[decl]
    fun cubeFrameBuffer(decl: CubeFrameBufferDeclaration): GlGpuCubeFrameBuffer? = cubeFrameBuffers[decl]
    fun gltf(decl: GltfDeclaration): GltfLoaded? = gltfs[decl]
}