package com.zakgof.korender.impl.material

import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.context.RoiTexturesContext
import com.zakgof.korender.math.Vec3
import impl.engine.Retentionable

internal class InternalPostShadingEffect(
    val name: String,
    val width: Int,
    val height: Int,
    val effectPassMaterialModifiers: List<InternalMaterialModifier>,
    val compositionColorOutput: String,
    val compositionDepthOutput: String,
    val compositionMaterialModifier: InternalMaterialModifier,
    override val retentionPolicy: RetentionPolicy
) : PostShadingEffect, Retentionable

internal class InternalRoiTexturesContext : RoiTexturesContext {

    private val rois = mutableListOf<Pair<Vec3, TextureDeclaration>>()

    override fun RoiTexture(u: Float, v: Float, scale: Float, texture: TextureDeclaration) {
        rois += Vec3(u, v, scale) to texture
    }

    fun collect(mb: MaterialBuilder) {
        mb.uniforms["roiCount"] = rois.size
        rois.forEachIndexed { i, roi ->
            mb.uniforms["roiTextures[$i]"] = roi.second
            mb.uniforms["roiuvs[$i]"] = roi.first
        }
    }

}