package com.zakgof.korender.context

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BaseParams
import com.zakgof.korender.BloomParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.FogParams
import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.GrassParams
import com.zakgof.korender.Image
import com.zakgof.korender.IndexType
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.OrthoProjectionDeclaration
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.SsrParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.StarrySkyParams
import com.zakgof.korender.TerrainParams
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.WaterParams
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.Deferred

interface KorenderContext {

    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: TouchHandler)
    fun OnKey(handler: KeyHandler)

    var camera: CameraDeclaration
    var projection: ProjectionDeclaration
    var background: ColorRGB

    val width: Int
    val height: Int

    fun texture(
        textureResource: String, filter: TextureFilter = TextureFilter.MipMap,
        wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024
    ): TextureDeclaration

    fun cubeTexture(
        nxResource: String, nyResource: String, nzResource: String,
        pxResource: String, pyResource: String, pzResource: String
    ): CubeTextureDeclaration

    fun cube(halfSide: Float = 0.5f): MeshDeclaration
    fun sphere(radius: Float = 1.0f): MeshDeclaration
    fun obj(objFile: String): MeshDeclaration
    fun screenQuad(): MeshDeclaration

    fun customMesh(
        id: Any,
        vertexCount: Int,
        indexCount: Int,
        vararg attributes: MeshAttribute,
        dynamic: Boolean = false,
        indexType: IndexType? = null,
        block: MeshInitializer.() -> Unit
    ): MeshDeclaration

    fun heightField(
        id: Any,
        cellsX: Int,
        cellsZ: Int,
        cellWidth: Float,
        height: (Int, Int) -> Float
    ): MeshDeclaration

    fun vertex(vertShaderFile: String): MaterialModifier
    fun fragment(fragShaderFile: String): MaterialModifier
    fun defs(vararg defs: String): MaterialModifier
    fun plugin(name: String, shaderFile: String): MaterialModifier
    fun standart(block: StandartParams.() -> Unit): MaterialModifier
    fun uniforms(block: BaseParams.() -> Unit): MaterialModifier
    fun terrain(block: TerrainParams.() -> Unit): MaterialModifier

    fun blurHorz(block: BlurParams.() -> Unit = {}): MaterialModifier
    fun blurVert(block: BlurParams.() -> Unit = {}): MaterialModifier
    fun adjust(block: AdjustParams.() -> Unit): MaterialModifier
    fun water(block: WaterParams.() -> Unit = {}): MaterialModifier
    fun fog(block: FogParams.() -> Unit = {}): MaterialModifier
    fun fxaa(): MaterialModifier

    fun fire(block: FireParams.() -> Unit = {}): MaterialModifier
    fun fireball(block: FireballParams.() -> Unit = {}): MaterialModifier
    fun smoke(block: SmokeParams.() -> Unit = {}): MaterialModifier
    fun grass(block: GrassParams.() -> Unit = {}): MaterialModifier

    fun fastCloudSky(block: FastCloudSkyParams.() -> Unit = {}): MaterialModifier
    fun starrySky(block: StarrySkyParams.() -> Unit = {}): MaterialModifier
    fun cubeSky(cubeTexture: CubeTextureDeclaration): MaterialModifier
    fun cubeSky(envSlot: Int): MaterialModifier

    fun ibl(env: CubeTextureDeclaration): MaterialModifier

    fun ssr(width: Int? = null, height: Int? = null, fxaa: Boolean = false, block: SsrParams.() -> Unit = {}): PostShadingEffect
    fun bloom(width: Int? = null, height: Int? = null, block: BloomParams.() -> Unit = {}): PostShadingEffect

    fun frustum(width: Float, height: Float, near: Float, far: Float): FrustumProjectionDeclaration
    fun ortho(width: Float, height: Float, near: Float, far: Float): OrthoProjectionDeclaration
    fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration

    fun loadImage(imageResource: String): Deferred<Image>

    fun vsm(blurRadius: Float? = null): ShadowAlgorithmDeclaration
    fun hard(): ShadowAlgorithmDeclaration
    fun pcss(samples: Int = 32, blurRadius: Float = 0.05f): ShadowAlgorithmDeclaration

    fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab
    fun grassPrefab(id: String, segments: Int, cell: Float, side: Int, filter: (Vec3) -> Boolean): Prefab

    fun positionInstancing(id: String, instanceCount: Int, dynamic: Boolean, block: InstancedRenderablesContext.() -> Unit): InstancingDeclaration

    val target: TargetPlatform

    enum class TargetPlatform {
        Desktop,
        Android,
        Web
    }
}