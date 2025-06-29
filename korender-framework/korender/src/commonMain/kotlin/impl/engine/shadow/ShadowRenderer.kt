package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.BaseMaterial
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.Scene
import com.zakgof.korender.impl.engine.TransientProperty
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.glgpu.Color4List
import com.zakgof.korender.impl.glgpu.FloatList
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuShadowTextureList
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.OrthoProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import impl.engine.ImmediatelyFreeRetentionPolicy
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

        val matrices = updateShadowCamera(scene.renderContext.projection, scene.renderContext.camera, lightDirection, declaration)
        val shadowCamera = matrices.first
        val shadowProjection = matrices.second

        val casterUniforms = mutableMapOf<String, Any?>()
        scene.renderContext.frameUniforms(casterUniforms)

        casterUniforms["view"] = shadowCamera.mat4
        casterUniforms["projection"] = shadowProjection.mat4
        casterUniforms["cameraPos"] = shadowCamera.position
        casterUniforms["cameraDir"] = shadowCamera.direction

        // TODO: UGLY!!!!
        scene.inventory.uniformBufferHolder.populateFrame({ casterUniforms[it] }, 0, true)

        frameBuffer.exec {
            scene.renderContext.state.set {
                clearColor(ColorRGBA(1f, 1f, 0f, 1f))
            }
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            shadowCasterDeclarations.filter {
                // TODO: renderable or material flag to disable shadow casting
                it.base == BaseMaterial.Renderable || it.base == BaseMaterial.Billboard
            }.forEach { renderableDeclaration ->

                val shadowMaterialModifier = InternalMaterialModifier {

                    it.vertShaderFile = if (renderableDeclaration.base == BaseMaterial.Billboard) "!shader/billboard.vert" else "!shader/base.vert"
                    it.fragShaderFile = "!shader/caster.frag"

                    if (declaration.fixedYRange != null) {
                        it.plugins["voutput"] = "!shader/plugin/voutput.fixedyrange.vert"
                        it.uniforms["fixedYMin"] = declaration.fixedYRange.first
                        it.uniforms["fixedYMax"] = declaration.fixedYRange.second
                    }
                    when (declaration.algorithm) {
                        is InternalVsmShadow -> it.shaderDefs += "VSM_SHADOW"
                        is InternalHardShadow -> it.shaderDefs += "HARD_SHADOW"
                        is InternalSoftwarePcfShadow -> it.shaderDefs += "PCSS_SHADOW"
                        is InternalHardwarePcfShadow -> it.shaderDefs += "HARDWARE_PCF_SHADOW"
                    }
                }

                val casterRenderableDeclaration = RenderableDeclaration(
                    renderableDeclaration.base,
                    renderableDeclaration.materialModifiers + shadowMaterialModifier,
                    renderableDeclaration.mesh,
                    renderableDeclaration.transform,
                    renderableDeclaration.retentionPolicy
                )

                scene.renderRenderable(casterRenderableDeclaration, shadowCamera, casterUniforms)
            }
            scene.inventory.uniformBufferHolder.flush()
        }

        if (declaration.algorithm is InternalVsmShadow && declaration.algorithm.blurRadius != null) {
            val texBlurRadius = declaration.algorithm.blurRadius * declaration.mapSize / shadowProjection.width
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
            SHADOW_SHIFTER * shadowProjection.mat4 * shadowCamera.mat4,
            listOf(
                if (index == 0) 0f else declaration.near,
                if (index == 0) 0f else declarations[index - 1].far,
                if (index == declarations.size - 1) 1e10f else declarations[index + 1].near,
                if (index == declarations.size - 1) 1e10f else declaration.far
            ),
            declaration.fixedYRange?.first ?: 0f,
            declaration.fixedYRange?.second ?: 0f,
            mode(declaration),
            (declaration.algorithm as? InternalSoftwarePcfShadow)?.samples ?: 0,
            when (declaration.algorithm) {
                is InternalSoftwarePcfShadow -> declaration.algorithm.blurRadius / shadowProjection.width
                is InternalHardwarePcfShadow -> declaration.algorithm.bias
                else -> 0f
            }
        )
    }

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
        texBlurRadius: Float
    ) {
        val uniforms = mutableMapOf<String, Any?>()

        val blurFrameBuffer = scene.inventory.frameBuffer(
            FrameBufferDeclaration("shadow-$id-blur", declaration.mapSize, declaration.mapSize, listOf(GlGpuTexture.Preset.VSM), true, TransientProperty(ImmediatelyFreeRetentionPolicy))
        ) ?: return

        val blurVQuadRenderableDeclaration = blurQuadRenderableDeclaration(texBlurRadius, "!shader/effect/blurv.frag")

        uniforms["colorTexture"] = frameBuffer.colorTextures[0]
        uniforms["depthTexture"] = frameBuffer.depthTexture

        blurFrameBuffer.exec {
            scene.renderContext.state.set { }
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            scene.renderRenderable(blurVQuadRenderableDeclaration, null, uniforms)
            scene.inventory.uniformBufferHolder.flush()
        }

        val blurHQuadRenderableDeclaration = blurQuadRenderableDeclaration(texBlurRadius, "!shader/effect/blurh.frag")

        uniforms["colorTexture"] = blurFrameBuffer.colorTextures[0]
        uniforms["depthTexture"] = blurFrameBuffer.depthTexture

        frameBuffer.exec {
            scene.renderContext.state.set { }
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            scene.renderRenderable(blurHQuadRenderableDeclaration, null, uniforms)
            scene.inventory.uniformBufferHolder.flush()
        }
    }

    private fun blurQuadRenderableDeclaration(texBlurRadius: Float, fragShader: String) = RenderableDeclaration(
        BaseMaterial.Screen,
        listOf(InternalMaterialModifier {
            it.vertShaderFile = "!shader/screen.vert"
            it.fragShaderFile = fragShader
            it.uniforms["radius"] = texBlurRadius
        }),
        ScreenQuad(ImmediatelyFreeRetentionPolicy),
        Transform.IDENTITY,
        ImmediatelyFreeRetentionPolicy
    )

    private fun updateShadowCamera(
        projection: Projection,
        camera: Camera,
        light: Vec3,
        declaration: CascadeDeclaration
    ): Pair<DefaultCamera, OrthoProjection> {

        projection as FrustumProjection

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

        val shadowProjection = OrthoProjection(dim, dim, near, far)
        val shadowCamera = DefaultCamera(cameraPos, light, up)

        return shadowCamera to shadowProjection
    }

    private fun frustumCorners(
        projection: FrustumProjectionDeclaration,
        camera: Camera,
        near: Float,
        far: Float
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
    val f1: Float
)

internal fun List<ShadowerData>.uniforms(m: MutableMap<String, Any?>, u: MutableMap<String, Any?>) {
    m["numShadows"] = size
    m["bsps[0]"] = Mat4List(this.map { it.bsp })
    m["cascade[0]"] = Color4List(this.map { ColorRGBA(it.cascade[0], it.cascade[1], it.cascade[2], it.cascade[3]) })
    m["yMin[0]"] = FloatList(this.map { it.yMin })
    m["yMax[0]"] = FloatList(this.map { it.yMax })
    m["shadowMode[0]"] = IntList(this.map { it.mode })
    m["i1[0]"] = IntList(this.map { it.i1 })
    m["f1[0]"] = FloatList(this.map { it.f1 })

    u["shadowTextures[0]"] = GlGpuTextureList(this.map { it.texture }, 5)
    u["pcfTextures[0]"] = GlGpuShadowTextureList(this.map { it.pcfTexture }, 5)
}
