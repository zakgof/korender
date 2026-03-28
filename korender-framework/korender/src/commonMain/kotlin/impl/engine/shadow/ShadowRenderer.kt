package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.KorenderException
import com.zakgof.korender.Platform
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.FrameContext
import com.zakgof.korender.impl.engine.FrameMaterialModifier
import com.zakgof.korender.impl.engine.ImmediatelyFreeRetentionPolicy
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.Scene
import com.zakgof.korender.impl.engine.TransientProperty
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.material.BlurMaterial
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.ceil
import kotlin.math.round

internal object ShadowRenderer {

    private val SHADOW_SHIFTER = Mat4(
        0.5f, 0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.0f, 0.5f,
        0.0f, 0.0f, 0.5f, 0.5f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    fun render(
        id: String,
        lightDirection: Vec3,
        declarations: List<CascadeDeclaration>,
        index: Int,
        shadowCasterDeclarations: List<RenderableDeclaration>,
        scene: Scene,
    ): ShadowerData? {

        val declaration = declarations[index]
        val frameBuffer = scene.inventory.frameBuffer(
            FrameBufferDeclaration("shadow-$id", declaration.mapSize, declaration.mapSize, fbPreset(declaration), true, TransientProperty(ImmediatelyFreeRetentionPolicy))
        ) ?: return null

        val shadowFrameMaterialModifier = updateShadowCamera(scene.renderContext, lightDirection, declaration)

        // TODO LMM is ugly
        scene.inventory.uniformBufferHolder.populateFrame(listOf(shadowFrameMaterialModifier, scene.lightMaterialModifier), true)

        frameBuffer.exec {
            scene.renderContext.state.set {
                clearColor(ColorRGBA(1f, 1f, 0f, 1f))
            }
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            shadowCasterDeclarations.filter {
                // TODO: renderable or material flag to disable shadow casting
                it.material is InternalBaseMaterial
            }.forEach { renderableDeclaration ->
                val casterModifier = CasterMaterialModifier(declaration)
                val casterRenderableDeclaration = RenderableDeclaration(
                    renderableDeclaration.material,
                    listOf(casterModifier),
                    renderableDeclaration.mesh,
                    renderableDeclaration.transform,
                    renderableDeclaration.transparent,
                    renderableDeclaration.retentionPolicy
                )
                scene.renderRenderable(casterRenderableDeclaration, shadowFrameMaterialModifier.frameContext.camera, isShadow = true)
            }
            scene.inventory.uniformBufferHolder.flush()
        }

        if (declaration.algorithm is InternalVsmShadow && declaration.algorithm.blurRadius != null) {
            val texBlurRadius = declaration.algorithm.blurRadius * declaration.mapSize / shadowFrameMaterialModifier.frameContext.projection.width
            blurShadowMap(
                id,
                declaration,
                frameBuffer,
                scene,
                texBlurRadius
            )
        }

        // TODO can only once on texture init
        if (declaration.algorithm is InternalHardwarePcfShadow) {
            frameBuffer.depthTexture!!.enablePcfMode()
        }

        return ShadowerData(
            outputShadowTexture(frameBuffer, declaration),
            outputPcfTexture(frameBuffer, declaration),
            SHADOW_SHIFTER * orthoMatrix(shadowFrameMaterialModifier.frameContext.projection) * shadowFrameMaterialModifier.frameContext.camera.mat4,
            listOf(
                if (index == 0) 0f else declaration.near,
                if (index == 0) 0f else declarations[index - 1].far,
                if (index == declarations.size - 1) declaration.far - (declaration.far - declaration.near) * 0.1f else declarations[index + 1].near,
                declaration.far
            ),
            declaration.fixedYRange?.first ?: 0f,
            declaration.fixedYRange?.second ?: 0f,
            mode(declaration),
            (declaration.algorithm as? InternalSoftwarePcfShadow)?.samples ?: 0,
            when (declaration.algorithm) {
                is InternalSoftwarePcfShadow -> declaration.algorithm.blurRadius / shadowFrameMaterialModifier.frameContext.projection.width
                is InternalHardwarePcfShadow -> declaration.algorithm.bias
                else -> 0f
            },
            when (declaration.algorithm) {
                is InternalSoftwarePcfShadow -> declaration.algorithm.bias
                else -> 0f
            }
        )
    }

    private fun orthoMatrix(projection: Projection) = Mat4(
        2f / projection.width, 0f, 0f, 0f,
        0f, 2f / projection.height, 0f, 0f,
        0f, 0f, -2f / (projection.far - projection.near), -(projection.far + projection.near) / (projection.far - projection.near),
        0f, 0f, 0f, 1f
    )

    private fun outputShadowTexture(frameBuffer: GlGpuFrameBuffer, declaration: CascadeDeclaration): GlGpuTexture? =
        when (declaration.algorithm) {
            is InternalVsmShadow -> frameBuffer.colorTextures[0]
            is InternalHardwarePcfShadow -> null
            else -> frameBuffer.depthTexture!!
        }

    private fun outputPcfTexture(frameBuffer: GlGpuFrameBuffer, declaration: CascadeDeclaration): GlGpuTexture? =
        if (declaration.algorithm is InternalHardwarePcfShadow) frameBuffer.depthTexture!! else null

    private fun fbPreset(declaration: CascadeDeclaration): List<GlGpuTexture.Preset> =
        if (declaration.algorithm is InternalVsmShadow) listOf(GlGpuTexture.Preset.VSM) else listOf()

    private fun mode(declaration: CascadeDeclaration): Int =
        when (declaration.algorithm) {
            is InternalHardShadow -> 0
            is InternalSoftwarePcfShadow -> 1
            is InternalVsmShadow -> 2
            is InternalHardwarePcfShadow -> 3
            else -> throw KorenderException("Unknown shadow algorithm")
        } or (if (declaration.fixedYRange != null) 128 else 0)

    private fun blurShadowMap(
        id: String,
        declaration: CascadeDeclaration,
        frameBuffer: GlGpuFrameBuffer,
        scene: Scene,
        texBlurRadius: Float,
    ) {
        val blurFrameBuffer = scene.inventory.frameBuffer(
            FrameBufferDeclaration("shadow-$id-blur", declaration.mapSize, declaration.mapSize, listOf(GlGpuTexture.Preset.VSM), true, TransientProperty(ImmediatelyFreeRetentionPolicy))
        ) ?: return

        val blurVQuadRenderableDeclaration = blurQuadRenderableDeclaration(texBlurRadius, true)

        scene.contextMaterialModifier.customTextureUniforms["colorTexture"] = frameBuffer.colorTextures[0]
        scene.contextMaterialModifier.customTextureUniforms["depthTexture"] = frameBuffer.depthTexture!!

        blurFrameBuffer.exec {
            scene.renderContext.state.set { }
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            scene.renderRenderable(blurVQuadRenderableDeclaration, null)
            scene.inventory.uniformBufferHolder.flush()
        }

        val blurHQuadRenderableDeclaration = blurQuadRenderableDeclaration(texBlurRadius, false)

        scene.contextMaterialModifier.customTextureUniforms["colorTexture"] = blurFrameBuffer.colorTextures[0]
        scene.contextMaterialModifier.customTextureUniforms["depthTexture"] = blurFrameBuffer.depthTexture!!

        frameBuffer.exec {
            scene.renderContext.state.set { }
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            scene.renderRenderable(blurHQuadRenderableDeclaration, null)
            scene.inventory.uniformBufferHolder.flush()
        }
    }

    private fun blurQuadRenderableDeclaration(texBlurRadius: Float, vertical: Boolean) = RenderableDeclaration(
        BlurMaterial(vertical, texBlurRadius),
        listOf(),
        ScreenQuad(ImmediatelyFreeRetentionPolicy),
        Transform.IDENTITY,
        false,
        ImmediatelyFreeRetentionPolicy
    )

    private fun updateShadowCamera(
        renderContext: RenderContext,
        light: Vec3,
        declaration: CascadeDeclaration,
    ): FrameMaterialModifier {

        val projection = renderContext.projection
        val camera = renderContext.camera
        val right = (light % 1.y).normalize()
        val up = (right % light).normalize()
        val corners = frustumCorners(projection, camera, declaration.near, declaration.far)
        val xmin = corners.minOf { it * right }
        val ymin = corners.minOf { it * up }
        val zmin = corners.minOf { it * light }
        val xmax = corners.maxOf { it * right }
        val ymax = corners.maxOf { it * up }
        val zmax = corners.maxOf { it * light }

        val farWidth = projection.width * declaration.far / projection.near
        val farHeight = projection.height * declaration.far / projection.near
        val depth = declaration.far - declaration.near
        val dim = Vec3(farHeight, farWidth, depth).length()

        val near = 1f
        val volume = zmax - zmin

        val fragSize = dim / declaration.mapSize * 2.0f
        val depthSize = volume / 255f

        val moveUpSnap = round((ymin + ymax) * 0.5f / fragSize) * fragSize
        val moveRightSnap = round((xmin + xmax) * 0.5f / fragSize) * fragSize
        val depthSnap = ceil(zmax / depthSize) * depthSize

        val centerBottom = right * moveRightSnap +
                up * moveUpSnap +
                light * depthSnap

        val far = near + volume
        val cameraPos = centerBottom - light * far

        val shadowProjection = Projection(dim, dim, near, far, OrthoProjectionMode)
        val shadowCamera = DefaultCamera(cameraPos, light, up)

        return FrameMaterialModifier(ShadowFrameContext(shadowProjection, shadowCamera, renderContext, declaration.mapSize))
    }

    private fun frustumCorners(
        projection: Projection,
        camera: Camera,
        near: Float,
        far: Float,
    ): List<Vec3> {
        camera as DefaultCamera
        val upNear = camera.up * (projection.height * 0.5f * near / projection.near)
        val rightNear =
            (camera.direction % camera.up).normalize() * (projection.width * 0.5f * near / projection.near)
        val toNear = camera.direction * near
        val toFar = camera.direction * far
        val upFar = upNear * (far / near)
        val rightFar = rightNear * (far / near)
        return listOf(
            camera.position + upNear + rightNear + toNear,
            camera.position - upNear + rightNear + toNear,
            camera.position - upNear - rightNear + toNear,
            camera.position + upNear - rightNear + toNear,
            camera.position + upFar + rightFar + toFar,
            camera.position - upFar + rightFar + toFar,
            camera.position - upFar - rightFar + toFar,
            camera.position + upFar - rightFar + toFar,
        )
    }
}

internal class ShadowerData(
    val texture: GlGpuTexture?,
    val pcfTexture: GlGpuTexture?,
    val bsp: Mat4,
    val cascade: List<Float>,
    val yMin: Float,
    val yMax: Float,
    val mode: Int,
    val i1: Int,
    val f1: Float,
    val f2: Float,
)

internal class ShadowFrameContext(
    override val projection: Projection,
    override val camera: Camera,
    val renderContext: RenderContext,
    mapSize: Int,
) : FrameContext {

    override val width = mapSize
    override val height = mapSize
    override val time
        get() = (Platform.nanoTime() - renderContext.frameInfoManager.startNanos) * 1e-9f
}

internal class CasterMaterialModifier(
    val declaration: CascadeDeclaration,
) : InternalMaterialModifier(
    "fixedYMin" to FloatGetter<CasterMaterialModifier> { it.declaration.fixedYRange!!.first },
    "fixedYMax" to FloatGetter<CasterMaterialModifier> { it.declaration.fixedYRange!!.second }
) {
    override val defs
        get() = super.defs + setOfNotNull(
            "SHADOW_CASTER",
            if (declaration.algorithm is InternalVsmShadow) "VSM_SHADOW" else null
        )

    override val plugins
        get() = super.plugins + listOfNotNull(declaration.fixedYRange?.let { "vprojection" to "!shader/plugin/vprojection.fixedyrange.vert" })
}
