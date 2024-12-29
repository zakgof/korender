package com.zakgof.korender.context

import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.AdjustParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.StandartMaterialOption
import com.zakgof.korender.StandartParams
import com.zakgof.korender.WaterParams

interface KorenderContext {

    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: (TouchEvent) -> Unit)

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
    fun options(vararg options: StandartMaterialOption): MaterialModifier
    fun standart(vararg options: StandartMaterialOption, block: StandartParams.() -> Unit): MaterialModifier

    fun blurHorz(block: BlurParams.() -> Unit = {}): MaterialModifier
    fun blurVert(block: BlurParams.() -> Unit = {}): MaterialModifier
    fun adjust(block: AdjustParams.() -> Unit): MaterialModifier
    fun fire(block: FireParams.() -> Unit = {}): MaterialModifier
    fun fireball(block: FireballParams.() -> Unit = {}): MaterialModifier
    fun smoke(block: SmokeParams.() -> Unit = {}): MaterialModifier
    fun water(block: WaterParams.() -> Unit = {}): MaterialModifier
    fun fxaa(): MaterialModifier
    fun fastCloudSky(block: FastCloudSkyParams.() -> Unit = {}): MaterialModifier

}