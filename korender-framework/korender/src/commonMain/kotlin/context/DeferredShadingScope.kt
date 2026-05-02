package com.zakgof.korender.context

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.DecalMaterial
import com.zakgof.korender.ShadingMaterialScope
import com.zakgof.korender.math.Vec3

interface DeferredShadingScope {

    /**
     * Defines modifiers for the shading step.
     *
     * @param shadingModifiers shading material modifiers
     */
    fun Shading(block: ShadingMaterialScope.() -> Unit = {})

    /**
     * Creates a screen-space reflection post shading effect for deferred rendering pipeline.
     * Experimental.
     *
     * @param downsample downsample factor for SSR framebuffer (normally 1, 2 or 4)
     * @param maxReflectionDistance maximum distance a reflected ray is traced
     * @param linearSteps number of forward raytracing steps
     * @param binarySteps number of binary search steps after the forward tracing
     * @param lastStepRatio factor to multiply forward raytracing step at maxReflectionDistance
     * @param envTexture cube texture declaration for environment reflections
     */
    fun Ssr(downsample: Int = 2, maxReflectionDistance: Float = 10f, linearSteps: Int = 64, binarySteps: Int = 5, lastStepRatio: Float = 4f, envTexture: CubeTextureDeclaration? = null)

    /**
     * Creates a bloom (glow) post shading effect for deferred rendering pipeline.
     * Experimental.
     *
     * @param threshold luminance threshold for pixels to glow
     * @param amount bloom intensity factor
     * @param radius bloom radius
     * @param downsample downsample factor for bloom framebuffer (normally 1, 2 or 4)
     */
    fun Bloom(threshold: Float = 0.9f, amount: Float = 3.0f, radius: Float = 16f, downsample: Int = 2)

    /**
     * Creates a bloom (glow) post shading effect with Kawase blur for deferred rendering pipeline.
     * Use this variant for wider bloom areas.
     * Experimental.
     *
     * @param threshold luminance threshold for pixels to glow
     * @param amount bloom intensity factor
     * @param downsample downsample factor for bloom framebuffer (normally 1, 2 or 4)
     * @param mips number of blur passes (normally 2-5)
     * @param offset downsampling kernel size
     * @param highResolutionRatio factor to include a higher-resolution buffer during the upsampling
     */
    fun BloomWide(threshold: Float = 0.9f, amount: Float = 3.0f, downsample: Int = 2, mips: Int = 3, offset: Float = 1.0f, highResolutionRatio: Float = 0.2f)

    /**
     * Enables screen-space ambient occlusion for deferred rendering pipeline.
     * Experimental.
     *
     * @param downsample downsample factor for the SSAO framebuffer (normally 1 or 2)
     * @param sampleCount number of AO samples
     * @param radius AO sampling radius in view-space units
     * @param bias depth bias to reduce self-occlusion
     * @param intensity overall darkness multiplier
     * @param blurRadius blur radius used by the SSAO smoothing pass
     */
    fun Ssao(
        downsample: Int = 2,
        sampleCount: Int = 16,
        radius: Float = 0.75f,
        bias: Float = 0.03f,
        intensity: Float = 1.0f,
        blurRadius: Float = 5f
    )

    /**
     * Enables horizon-based ambient occlusion for deferred rendering pipeline.
     * Experimental.
     *
     * @param downsample downsample factor for the HBAO framebuffer (normally 1 or 2)
     * @param sampleCount number of horizon samples
     * @param radius AO sampling radius in view-space units
     * @param bias horizon bias to reduce self-occlusion
     * @param intensity overall darkness multiplier
     * @param blurRadius blur radius used by the HBAO smoothing pass
     */
    fun Hbao(
        downsample: Int = 2,
        sampleCount: Int = 16,
        radius: Float = 0.75f,
        bias: Float = 0.02f,
        intensity: Float = 1.0f,
        blurRadius: Float = 5f
    )

    /**
     * Creates a decal.
     *
     * @param materialModifiers material modifiers
     * @param position decal application position
     * @param look decal application direction
     * @param up up direction for decal application (corresponds to y axis of decal texture)
     * @param size decal quad size, in world space units
     */
    fun Decal(material: DecalMaterial, position: Vec3, look: Vec3, up: Vec3, size: Float)
}
