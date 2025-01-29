package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.glgpu.ColorList
import com.zakgof.korender.impl.glgpu.FloatList
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.glgpu.IntList
import com.zakgof.korender.impl.glgpu.Mat4List
import com.zakgof.korender.impl.material.InternalBlurParams
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.MaterialBuilder
import com.zakgof.korender.impl.material.ParamUniforms
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.OrthoProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Mat4
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
        inventory: Inventory,
        lightDirection: Vec3,
        declarations: List<CascadeDeclaration>,
        index: Int,
        renderContext: RenderContext,
        shadowCasterDeclarations: List<RenderableDeclaration>,
        fixer: (Any?) -> Any?
    ): ShadowerData? {

        val declaration = declarations[index]
        val frameBuffer = inventory.frameBuffer(
            FrameBufferDeclaration("shadow-$id", declaration.mapSize, declaration.mapSize, fbPreset(declaration), true)
        ) ?: return null

        val matrices = updateShadowCamera(renderContext.projection, renderContext.camera, lightDirection, declaration)
        val shadowCamera = matrices.first
        val shadowProjection = matrices.second

        val casterUniforms = mutableMapOf<String, Any?>()
        renderContext.uniforms(casterUniforms)

        casterUniforms["view"] = shadowCamera.mat4
        casterUniforms["projection"] = shadowProjection.mat4
        casterUniforms["cameraPos"] = shadowCamera.position
        casterUniforms["cameraDir"] = shadowCamera.direction

        frameBuffer.exec {
            glClearColor(1f, 1f, 0f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            shadowCasterDeclarations.filter {
                // TODO: renderable or material flag to disable shadow casting
                (it.shader.fragFile == "!shader/geometry.frag" ||
                        it.shader.fragFile == "!shader/forward.frag")
            }.forEach { renderableDeclaration ->
                val mesh = inventory.mesh(renderableDeclaration.mesh)
                val uniforms = mutableMapOf<String, Any?>()
                val defs = mutableSetOf<String>()
                defs += renderableDeclaration.shader.defs
                if (declaration.fixedYRange != null) {
                    defs += "FIXED_SHADOW_Y_RANGE"
                    uniforms["fixedYMin"] = declaration.fixedYRange.first
                    uniforms["fixedYMax"] = declaration.fixedYRange.second
                }
                when (declaration.algorithm) {
                    is InternalVsmParams -> defs += "VSM_SHADOW"
                    is InternalHardParams -> defs += "HARD_SHADOW"
                    is InternalPcssParams -> defs += "PCSS_SHADOW"
                }

                val modifiedShaderDeclaration = ShaderDeclaration(
                    "!shader/caster.vert", "!shader/caster.frag",
                    defs,
                    renderableDeclaration.shader.options,
                    renderableDeclaration.shader.plugins
                )
                val shader = inventory.shader(modifiedShaderDeclaration)
                if (mesh != null && shader != null) {
                    Renderable(mesh, shader, renderableDeclaration.uniforms, renderableDeclaration.transform)
                        .render(casterUniforms + uniforms, fixer)
                }
            }
        }

        if (declaration.algorithm is InternalVsmParams && declaration.algorithm.blurRadius != null) {
            val texBlurRadius = declaration.algorithm.blurRadius * declaration.mapSize / shadowProjection.width
            blurShadowMap(
                id,
                declaration,
                frameBuffer,
                inventory,
                renderContext,
                fixer,
                texBlurRadius
            )
        }
        return ShadowerData(
            output(frameBuffer, declaration),
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
            (declaration.algorithm as? InternalPcssParams)?.samples ?: 0,
            (declaration.algorithm as? InternalPcssParams)?.blurRadius ?: 0f
        )
    }

    private fun output(frameBuffer: GlGpuFrameBuffer, declaration: CascadeDeclaration): GlGpuTexture =
        if (declaration.algorithm is InternalVsmParams) frameBuffer.colorTextures[0] else frameBuffer.depthTexture!!

    private fun fbPreset(declaration: CascadeDeclaration): List<GlGpuTexture.Preset> =
        if (declaration.algorithm is InternalVsmParams) listOf(GlGpuTexture.Preset.VSM) else listOf()

    private fun mode(declaration: CascadeDeclaration): Int =
        when (declaration.algorithm) {
            is InternalHardParams -> 0
            is InternalPcssParams -> 1
            is InternalVsmParams -> 2
            else -> 0
        } or (if (declaration.fixedYRange != null) 128 else 0)

    private fun blurShadowMap(
        id: String,
        declaration: CascadeDeclaration,
        frameBuffer: GlGpuFrameBuffer,
        inventory: Inventory,
        renderContext: RenderContext,
        fixer: (Any?) -> Any?,
        texBlurRadius: Float
    ) {

        val uniforms = mutableMapOf<String, Any?>()
        renderContext.uniforms(uniforms) // TODO once per frame only

        val blurFrameBuffer = inventory.frameBuffer(
            FrameBufferDeclaration("shadow-$id-blur", declaration.mapSize, declaration.mapSize, listOf(GlGpuTexture.Preset.VSM), true)
        ) ?: return

        val blur1 = materialDeclaration(MaterialBuilder(false),
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/screen.vert"
                it.fragShaderFile = "!shader/effect/blurv.frag"
                it.shaderUniforms = ParamUniforms(InternalBlurParams()) {
                    radius = texBlurRadius
                }
            }
        )
        uniforms["filterColorTexture"] = frameBuffer.colorTextures[0]
        uniforms["filterDepthTexture"] = frameBuffer.depthTexture

        blurFrameBuffer.exec {
            val mesh = inventory.mesh(ScreenQuad)
            val shader = inventory.shader(blur1.shader)
            glClearColor(0f, 0f, 0f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            if (mesh != null && shader != null) {
                Renderable(mesh, shader, blur1.uniforms).render(uniforms, fixer)
            }
        }
        val blur2 = materialDeclaration(MaterialBuilder(false),
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/screen.vert"
                it.fragShaderFile = "!shader/effect/blurh.frag"
                it.shaderUniforms = ParamUniforms(InternalBlurParams()) {
                    radius = texBlurRadius
                }
            }
        )
        uniforms["filterColorTexture"] = frameBuffer.colorTextures[0]
        uniforms["filterDepthTexture"] = frameBuffer.depthTexture

        frameBuffer.exec {
            val mesh = inventory.mesh(ScreenQuad)
            val shader = inventory.shader(blur2.shader)
            glClearColor(0f, 0f, 0f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            if (mesh != null && shader != null) {
                Renderable(mesh, shader, blur1.uniforms).render(uniforms, fixer)
            }
        }
    }

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
    val texture: GlGpuTexture,
    val bsp: Mat4,
    val cascade: List<Float>,
    val yMin: Float,
    val yMax: Float,
    val mode: Int,
    val i1: Int,
    val f1: Float
)

internal fun List<ShadowerData>.uniforms(m: MutableMap<String, Any?>) {
    m["numShadows"] = size
    m["shadowTextures[0]"] = GlGpuTextureList(this.map { it.texture })
    m["bsps[0]"] = Mat4List(this.map { it.bsp })
    m["cascade[0]"] = ColorList(this.map { Color(it.cascade[3], it.cascade[0], it.cascade[1], it.cascade[2]) })
    m["yMin[0]"] = FloatList(this.map { it.yMin })
    m["yMax[0]"] = FloatList(this.map { it.yMax })
    m["shadowMode[0]"] = IntList(this.map { it.mode })
    m["i1[0]"] = IntList(this.map { it.i1 })
    m["f1[0]"] = FloatList(this.map { it.f1 })
}
