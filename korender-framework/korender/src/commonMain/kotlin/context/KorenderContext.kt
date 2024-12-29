package com.zakgof.korender.context

import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchEvent

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
}