package com.zakgof.korender.impl.engine

import com.zakgof.korender.ShaderPlugin
import com.zakgof.korender.ShaderPluginId
import com.zakgof.korender.impl.font.Font
import com.zakgof.korender.impl.font.Fonts
import com.zakgof.korender.impl.font.InternalFontDeclaration
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.MeshLink
import com.zakgof.korender.impl.glgpu.GlBindableTexture
import com.zakgof.korender.impl.glgpu.GlGpuCubeFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture3D
import com.zakgof.korender.impl.material.GpuTextureLink
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.InternalTexture
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.ShaderPluginRegistry
import com.zakgof.korender.impl.material.Shaders
import com.zakgof.korender.impl.material.TextureLinkDeclaration
import com.zakgof.korender.impl.material.Texturing
import com.zakgof.korender.impl.material.UniformBufferHolder
import com.zakgof.korender.impl.model.InternalModel
import com.zakgof.korender.impl.model.ModelFactory
import com.zakgof.korender.impl.model.terrain.Clipmaps

internal class ShaderServices(
    val zeroTex: GlGpuTexture = GlGpuTexture.zeroTex(),
    val zeroShadowTex: GlGpuTexture = GlGpuTexture.zeroShadowTex(),
    val uboHolder: UniformBufferHolder = UniformBufferHolder(),
    val shaderPluginRegistry: ShaderPluginRegistry = ShaderPluginRegistry(),
    val textureBindingCache: TextureBindingCache = TextureBindingCache()
)

internal class Inventory(private val loader: Loader) {

    val shaderServices = ShaderServices()

    private val meshes = Registry<InternalMeshDeclaration, MeshLink> { Geometry.create(it, loader, it.nodeContext) }
    private val shaders = Registry<ShaderDeclaration, GlGpuShader> { Shaders.create(shaderServices, it, loader, it.nodeContext.resourceLoader) }
    private val textures = Registry<InternalTexture, GlBindableTexture> { it.generateGpuTexture(loader) }
    private val textures3D = Registry<ImageTexture3DDeclaration, GlGpuTexture3D> { it.generateGpuTexture3D(loader) }

    private val textureLinks = Registry<TextureLinkDeclaration, GpuTextureLink> { it.generateGpuTextureLink(loader) }
    private val resourceCubeTextures = Registry<ResourceCubeTextureDeclaration, GlGpuCubeTexture> { Texturing.cube(it, loader) }
    private val imageCubeTextures = Registry<ImageCubeTextureDeclaration, GlGpuCubeTexture> { Texturing.cube(it) }
    private val fonts = Registry<InternalFontDeclaration, Font> { Fonts.load(it.resource, loader, it.nodeContext) }
    private val frameBuffers = Registry<FrameBufferDeclaration, GlGpuFrameBuffer> { GlGpuFrameBuffer(it.id, it.width, it.height, it.colorTexturePresets, it.withDepth) }
    private val cubeFrameBuffers = Registry<CubeFrameBufferDeclaration, GlGpuCubeFrameBuffer> { GlGpuCubeFrameBuffer(it.id, it.width, it.height, it.withDepth) }
    private val heightFields = Registry<HeightFieldDeclaration, Clipmaps> { Clipmaps(it) }
    private val models = Registry<ModelDeclaration, InternalModel> { ModelFactory.load(it, loader) }

    private val registries = listOf(meshes, shaders, textures, textures3D, textureLinks, resourceCubeTextures, imageCubeTextures, fonts, frameBuffers, cubeFrameBuffers, heightFields, models)

    fun go(time: Float, generation: Int, block: Inventory.() -> Boolean) {
        registries.forEach { it.begin() }
        textures.forEachKey { it.updateTexture(loader) }
        val ok = block.invoke(this)
        if (ok) {
            registries.forEach { it.end(time, generation) }
        }
    }

    fun pending() = loader.pending()

    fun mesh(decl: InternalMeshDeclaration): MeshLink? = meshes[decl]
    fun shader(decl: ShaderDeclaration): GlGpuShader? = shaders[decl]
    fun texture(decl: InternalTexture): GlBindableTexture? = textures[decl]
    fun texture3D(decl: ImageTexture3DDeclaration): GlGpuTexture3D? = textures3D[decl]
    fun textureLink(decl: TextureLinkDeclaration): GpuTextureLink? = textureLinks[decl]
    fun cubeTexture(decl: ResourceCubeTextureDeclaration): GlGpuCubeTexture? = resourceCubeTextures[decl]
    fun cubeTexture(decl: ImageCubeTextureDeclaration): GlGpuCubeTexture? = imageCubeTextures[decl]
    fun font(decl: InternalFontDeclaration): Font? = fonts[decl]
    fun frameBuffer(decl: FrameBufferDeclaration): GlGpuFrameBuffer? = frameBuffers[decl]
    fun cubeFrameBuffer(decl: CubeFrameBufferDeclaration): GlGpuCubeFrameBuffer? = cubeFrameBuffers[decl]
    fun heightField(decl: HeightFieldDeclaration): Clipmaps? = heightFields[decl]
    fun model(decl: ModelDeclaration) : InternalModel? = models[decl]

    fun shaderPlugin(id: ShaderPluginId, file: String): ShaderPlugin =
        shaderServices.shaderPluginRegistry.registerCustom(id, file)
}

