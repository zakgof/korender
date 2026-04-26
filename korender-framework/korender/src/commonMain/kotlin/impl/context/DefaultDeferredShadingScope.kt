package com.zakgof.korender.impl.context

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.DecalMaterial
import com.zakgof.korender.ShadingMaterialScope
import com.zakgof.korender.context.DeferredShadingScope
import com.zakgof.korender.impl.engine.DeferredShadingDeclaration
import com.zakgof.korender.impl.engine.InternalDecalDeclaration
import com.zakgof.korender.impl.material.InternalDecalMaterial
import com.zakgof.korender.impl.material.InternalShadingMaterial
import com.zakgof.korender.impl.material.bloomMipEffect
import com.zakgof.korender.impl.material.bloomSimpleEffect
import com.zakgof.korender.impl.material.ssaoEffect
import com.zakgof.korender.impl.material.ssrEffect
import com.zakgof.korender.math.Vec3

internal class DefaultDeferredShadingScope(private var deferredShadingDeclaration: DeferredShadingDeclaration) : DeferredShadingScope {

    override fun Shading(block: ShadingMaterialScope.() -> Unit) {
        deferredShadingDeclaration.shadingMaterial = InternalShadingMaterial().also { block.invoke(it) }
    }

    override fun Ssr(
        downsample: Int,
        maxReflectionDistance: Float,
        linearSteps: Int,
        binarySteps: Int,
        lastStepRatio: Float,
        envTexture: CubeTextureDeclaration?
    ) {
        deferredShadingDeclaration.postShadingEffects += ssrEffect(
            downsample,
            maxReflectionDistance,
            linearSteps,
            binarySteps,
            lastStepRatio,
            envTexture,
            deferredShadingDeclaration.nodeContext
        )
    }

    override fun Bloom(threshold: Float, amount: Float, radius: Float, downsample: Int) {
        deferredShadingDeclaration.postShadingEffects += bloomSimpleEffect(
            threshold,
            amount,
            radius,
            downsample,
            deferredShadingDeclaration.nodeContext
        )
    }

    override fun BloomWide(threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float) {
        deferredShadingDeclaration.postShadingEffects += bloomMipEffect(
            threshold,
            amount,
            downsample,
            mips,
            offset,
            highResolutionRatio,
            deferredShadingDeclaration.nodeContext
        )
    }

    override fun Ssao(downsample: Int, sampleCount: Int, radius: Float, bias: Float, intensity: Float, blurRadius: Float) {
        deferredShadingDeclaration.shadingEffects += ssaoEffect(downsample, sampleCount, radius, bias, intensity, blurRadius, deferredShadingDeclaration.nodeContext)
    }

    override fun Decal(material: DecalMaterial, position: Vec3, look: Vec3, up: Vec3, size: Float) {
        deferredShadingDeclaration.decals += InternalDecalDeclaration(position, look, up, size, material as InternalDecalMaterial)
    }

}
