package com.zakgof.korender.impl.engine

import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.font.Font
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.font.InternalFontDeclaration
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.MeshLink
import com.zakgof.korender.impl.glgpu.GlGpuCubeFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture3D
import com.zakgof.korender.impl.gltf.GltfLoaded
import com.zakgof.korender.impl.gltf.GltfLoader
import com.zakgof.korender.impl.material.GpuTextureLink
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.impl.material.TextureLinkDeclaration
import com.zakgof.korender.impl.material.Texturing
import com.zakgof.korender.impl.material.UniformBufferHolder

internal class Inventory(appResourceLoader: ResourceLoader) {

    private val loader = Loader(appResourceLoader)

    private val zeroTex = GlGpuTexture.zeroTex()
    private val zeroShadowTex = GlGpuTexture.zeroShadowTex()

    val uniformBufferHolder = UniformBufferHolder()

    private val meshes = Registry<InternalMeshDeclaration, MeshLink> { Geometry.create(it, loader) }
    private val shaders = Registry<ShaderDeclaration, GlGpuShader> { Shaders.create(it, loader, zeroTex, zeroShadowTex, uniformBufferHolder) }
    private val textures = Registry<InternalTexture, GlGpuTexture> { it.generateGpuTexture(loader) }
    private val textures3D = Registry<ImageTexture3DDeclaration, GlGpuTexture3D> { it.generateGpuTexture3D(loader) }

    private val textureLinks = Registry<TextureLinkDeclaration, GpuTextureLink> { it.generateGpuTextureLink(loader) }
    private val resourceCubeTextures = Registry<ResourceCubeTextureDeclaration, GlGpuCubeTexture> { Texturing.cube(it, loader) }
    private val imageCubeTextures = Registry<ImageCubeTextureDeclaration, GlGpuCubeTexture> { Texturing.cube(it) }
    private val fonts = Registry<InternalFontDeclaration, Font> { Fonts.load(it.resource, loader) }
    private val frameBuffers = Registry<FrameBufferDeclaration, GlGpuFrameBuffer> { GlGpuFrameBuffer(it.id, it.width, it.height, it.colorTexturePresets, it.withDepth) }
    private val cubeFrameBuffers = Registry<CubeFrameBufferDeclaration, GlGpuCubeFrameBuffer> { GlGpuCubeFrameBuffer(it.id, it.width, it.height, it.withDepth) }
    private val gltfs = Registry<GltfDeclaration, GltfLoaded> { GltfLoader.load(it, loader) }

    private val registries = listOf(meshes, shaders, textures, textures3D, textureLinks, resourceCubeTextures, imageCubeTextures, fonts, frameBuffers, cubeFrameBuffers, gltfs)

    fun go(time: Float, generation: Int, block: Inventory.() -> Boolean) {
        registries.forEach { it.begin() }
        val ok = block.invoke(this)
        if (ok) {
            registries.forEach { it.end(time, generation) }
        }
    }

    fun pending() = loader.pending()

    fun mesh(decl: InternalMeshDeclaration): MeshLink? = meshes[decl]
    fun shader(decl: ShaderDeclaration): GlGpuShader? = shaders[decl]
    fun texture(decl: InternalTexture): GlGpuTexture? = textures[decl]
    fun texture3D(decl: ImageTexture3DDeclaration): GlGpuTexture3D? = textures3D[decl]
    fun textureLink(decl: TextureLinkDeclaration): GpuTextureLink? = textureLinks[decl]
    fun cubeTexture(decl: ResourceCubeTextureDeclaration): GlGpuCubeTexture? = resourceCubeTextures[decl]
    fun cubeTexture(decl: ImageCubeTextureDeclaration): GlGpuCubeTexture? = imageCubeTextures[decl]
    fun font(decl: InternalFontDeclaration): Font? = fonts[decl]
    fun frameBuffer(decl: FrameBufferDeclaration): GlGpuFrameBuffer? = frameBuffers[decl]
    fun cubeFrameBuffer(decl: CubeFrameBufferDeclaration): GlGpuCubeFrameBuffer? = cubeFrameBuffers[decl]
    fun gltf(decl: GltfDeclaration): GltfLoaded? = gltfs[decl]

    fun onWaitUpdate(block: () -> Unit) = loader.onWaitUpdate(block)
}