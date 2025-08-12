package com.zakgof.korender.context

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.Image
import com.zakgof.korender.IndexType
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import kotlinx.coroutines.Deferred

interface KorenderContext {

    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: TouchHandler)
    fun OnKey(handler: KeyHandler)

    var camera: CameraDeclaration
    var projection: ProjectionDeclaration
    var background: ColorRGBA
    var retentionPolicy: RetentionPolicy
    var retentionGeneration: Int

    val width: Int
    val height: Int

    fun <T> load(resource: String, mapper: (ByteArray) -> T) : Deferred<T>

    fun texture(textureResource: String, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration
    fun texture(id: String, image: Image, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration
    fun textureProbe(frameProbeName: String): TextureDeclaration

    fun cubeTexture(resources: CubeTextureResources): CubeTextureDeclaration
    fun cubeTexture(id: String, images: CubeTextureImages): CubeTextureDeclaration
    fun cubeTextureProbe(envProbeName: String): CubeTextureDeclaration

    fun captureEnv(resolution: Int, near: Float, far: Float, position: Vec3 = ZERO, insideOut: Boolean = false, block: FrameContext.() -> Unit): CubeTextureImages
    fun captureFrame(width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameContext.() -> Unit): Image

    fun quad(halfSideX: Float = 0.5f, halfSideY: Float = 0.5f): MeshDeclaration
    fun biQuad(halfSideX: Float = 0.5f, halfSideY: Float = 0.5f): MeshDeclaration
    fun cube(halfSide: Float = 0.5f): MeshDeclaration
    fun sphere(radius: Float = 1.0f, slices: Int = 32, sectors: Int = 32): MeshDeclaration
    fun disk(radius: Float = 1f, sectors: Int = 32): MeshDeclaration
    fun coneTop(height: Float = 1f, radius: Float = 1f, sectors: Int = 32): MeshDeclaration
    fun cylinderSide(height: Float = 1f, radius: Float = 1f, sectors: Int = 32): MeshDeclaration
    fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration
    fun obj(objFile: String): MeshDeclaration
    fun pipeMesh(id: String, segments: Int, dynamic: Boolean = false, block: PipeMeshContext.() -> Unit): MeshDeclaration

    fun mesh(id: String, mesh: Mesh): MeshDeclaration
    fun customMesh(id: String, vertexCount: Int, indexCount: Int, vararg attributes: MeshAttribute<*>, dynamic: Boolean = false, indexType: IndexType? = null, block: MeshInitializer.() -> Unit): MeshDeclaration

    fun vertex(vertShaderFile: String): MaterialModifier
    fun fragment(fragShaderFile: String): MaterialModifier
    fun defs(vararg defs: String): MaterialModifier
    fun plugin(name: String, shaderFile: String): MaterialModifier
    fun uniforms(vararg pairs: Pair<String, Any?>): MaterialModifier

    fun base(color: ColorRGBA = ColorRGBA.White, colorTexture: TextureDeclaration? = null, metallicFactor: Float = 0.1f, roughnessFactor: Float = 0.5f): MaterialModifier
    fun triplanar(scale: Float = 1.0f): MaterialModifier
    fun normalTexture(normalTexture: TextureDeclaration): MaterialModifier
    fun emission(factor: ColorRGB): MaterialModifier
    fun metallicRoughnessTexture(texture: TextureDeclaration): MaterialModifier
    fun specularGlossiness(specularFactor: ColorRGB, glossinessFactor: Float): MaterialModifier
    fun specularGlossinessTexture(texture: TextureDeclaration): MaterialModifier

    fun billboard(position: Vec3 = ZERO, scale: Vec2 = Vec2(1f, 1f), rotation: Float = 0.0f): MaterialModifier

    fun terrain(heightTexture: TextureDeclaration, heightTextureSize: Int, heightScale: Float, outsideHeight: Float, terrainCenter: Vec3 = ZERO): MaterialModifier
    fun pipe(): MaterialModifier
    fun radiant(radiantTexture: CubeTextureDeclaration, radiantNormalTexture: CubeTextureDeclaration, colorTexture: CubeTextureDeclaration, normalTexture: CubeTextureDeclaration): MaterialModifier

    fun radiantCapture(radiantMax: Float): MaterialModifier
    fun normalCapture(): MaterialModifier

    fun blurHorz(radius: Float): MaterialModifier
    fun blurVert(radius: Float): MaterialModifier
    fun adjust(brightness: Float = 0.0f, contrast: Float = 1.0f, saturation: Float = 1.0f): MaterialModifier
    fun water(waterColor: ColorRGB = ColorRGB(0x00182A), transparency: Float = 0.1f, waveScale: Float = 25.0f, waveMagnitude: Float = 0.3f): MaterialModifier
    fun fog(density: Float = 0.00003f, color: ColorRGB = ColorRGB(0xB8CAE9)): MaterialModifier
    fun fxaa(): MaterialModifier

    fun fire(strength: Float = 3.0f): MaterialModifier
    fun fireball(power: Float = 0.5f): MaterialModifier
    fun smoke(density: Float = 0.5f, seed: Float = 0f): MaterialModifier

    fun fastCloudSky(density: Float = 3.0f, thickness: Float = 10.0f, scale: Float = 1.0f, rippleAmount: Float = 0.3f, rippleScale: Float = 4.0f, zenithColor: ColorRGB = ColorRGB(0x3F6FC3), horizonColor: ColorRGB = ColorRGB(0xB8CAE9), cloudLight: Float = 1.0f, cloudDark: Float = 0.7f): MaterialModifier
    fun starrySky(colorness: Float = 0.8f, density: Float = 20.0f, speed: Float = 1.0f, size: Float = 15.0f): MaterialModifier
    fun cubeSky(cubeTexture: CubeTextureDeclaration): MaterialModifier
    fun textureSky(texture: TextureDeclaration): MaterialModifier

    fun ibl(env: CubeTextureDeclaration): MaterialModifier
    fun ibl(env: MaterialModifier): MaterialModifier

    fun roiTextures(block: RoiTexturesContext.() -> Unit): MaterialModifier

    fun ssr(width: Int? = null, height: Int? = null, fxaa: Boolean = false, maxRayTravel: Float = 10f, linearSteps: Int = 12, binarySteps: Int = 5, envTexture: CubeTextureDeclaration? = null): PostShadingEffect
    fun bloom(width: Int? = null, height: Int? = null): PostShadingEffect

    fun projection(width: Float, height: Float, near: Float, far: Float, mode: ProjectionMode = frustum()): ProjectionDeclaration
    fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration
    fun frustum(): ProjectionMode
    fun ortho(): ProjectionMode
    fun log(c: Float = 1.0f): ProjectionMode

    fun createImage(width: Int, height: Int, format: Image.Format): Image
    fun loadImage(imageResource: String): Deferred<Image>
    fun loadImage(bytes: ByteArray, type: String): Deferred<Image>

    fun vsm(blurRadius: Float? = null): ShadowAlgorithmDeclaration
    fun hard(): ShadowAlgorithmDeclaration
    fun softwarePcf(samples: Int = 8, blurRadius: Float = 0.005f): ShadowAlgorithmDeclaration
    fun hardwarePcf(bias: Float = 0.005f): ShadowAlgorithmDeclaration

    fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab

    fun instancing(id: String, count: Int, dynamic: Boolean, block: InstancedRenderablesContext.() -> Unit): InstancingDeclaration
    fun gltfInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedGltfContext.() -> Unit): GltfInstancingDeclaration
    fun billboardInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedBillboardsContext.() -> Unit): BillboardInstancingDeclaration

    val target: TargetPlatform

    enum class TargetPlatform {
        Desktop,
        Android,
        Web
    }
}