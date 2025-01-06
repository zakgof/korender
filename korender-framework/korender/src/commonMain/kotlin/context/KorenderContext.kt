package com.zakgof.korender.context

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.Image
import com.zakgof.korender.IndexType
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.OrthoProjectionDeclaration
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.RenderingOption
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.StarrySkyParams
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.WaterParams
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.Deferred

interface KorenderContext {

    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: (TouchEvent) -> Unit)

    var camera: CameraDeclaration
    var projection: ProjectionDeclaration
    var background: Color

    val width: Int
    val height: Int

    fun texture(
        textureResource: String,
        filter: TextureFilter = TextureFilter.MipMapLinearLinear,
        wrap: TextureWrap = TextureWrap.Repeat,
        aniso: Int = 1024
    ): TextureDeclaration

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
    fun options(vararg options: RenderingOption): MaterialModifier
    fun standart(vararg options: RenderingOption, block: StandartParams.() -> Unit): MaterialModifier

    fun blurHorz(block: BlurParams.() -> Unit = {}): MaterialModifier
    fun blurVert(block: BlurParams.() -> Unit = {}): MaterialModifier
    fun adjust(block: AdjustParams.() -> Unit): MaterialModifier
    fun fire(block: FireParams.() -> Unit = {}): MaterialModifier
    fun fireball(block: FireballParams.() -> Unit = {}): MaterialModifier
    fun smoke(block: SmokeParams.() -> Unit = {}): MaterialModifier
    fun water(block: WaterParams.() -> Unit = {}): MaterialModifier
    fun fxaa(): MaterialModifier
    fun fastCloudSky(block: FastCloudSkyParams.() -> Unit = {}): MaterialModifier
    fun starrySky(block: StarrySkyParams.() -> Unit = {}): MaterialModifier

    fun frustum(width: Float, height: Float, near: Float, far: Float): FrustumProjectionDeclaration
    fun ortho(width: Float, height: Float, near: Float, far: Float): OrthoProjectionDeclaration
    fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration

    fun loadImage(imageResource: String): Deferred<Image>
}