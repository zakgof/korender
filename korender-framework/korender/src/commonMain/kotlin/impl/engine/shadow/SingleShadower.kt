package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.impl.engine.ShaderDeclaration
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection

internal class SingleShadower(
    private val index: Int,
    private val inventory: Inventory,
    private val decl: CascadeDeclaration
) : Shadower {

    override val cascadeNumber = 1
    private val frameBuffer: GlGpuFrameBuffer? = inventory.frameBuffer(
        FrameBufferDeclaration(
            "shadow$index",
            decl.mapSize,
            decl.mapSize,
            false
        )
    )

    override fun render(
        renderContext: RenderContext,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): Map<String, Any?> {

        if (frameBuffer == null)
            return mapOf();

        val matrices = updateShadowCamera(renderContext.projection, renderContext.camera, renderContext.lightDirection)
        val shadowCamera = matrices.first
        val shadowProjection = matrices.second
        val casterUniforms = renderContext.uniforms() + mapOf(
            "view" to shadowCamera.mat4,
            "projection" to shadowProjection.mat4,
            "cameraPos" to shadowCamera.position
        )
        frameBuffer.exec {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glEnable(GL_DEPTH_TEST)
            glCullFace(GL_BACK)
            shadowCasters.forEach { casterRenderable ->
                // TODO: need to copy all the defs and plugins from the original shader
                val casterShader = inventory.shader(
                    ShaderDeclaration(
                        "!shader/standart.vert",
                        "!shader/standart.frag",
                        setOf("SHADOW_CASTER")
                    )
                )

                casterShader?.let {
                    casterRenderable.render(casterUniforms, fixer, it)
                }
            }
        }

        return mapOf(
            "shadowTexture$index" to frameBuffer.colorTexture,
            "shadowProjection$index" to shadowProjection.mat4,
            "shadowView$index" to shadowCamera.mat4
        )
    }

    private fun updateShadowCamera(
        projection: Projection,
        camera: Camera,
        light: Vec3
    ): Pair<DefaultCamera, OrthoProjection> {

        val right = (light % 1.y).normalize()
        val up = (right % light).normalize()
        val corners = frustumCorners(projection, camera, decl.near, decl.far)
        val xmin = corners.minOf { it * right }
        val ymin = corners.minOf { it * up }
        val zmin = corners.minOf { it * light }
        val xmax = corners.maxOf { it * right }
        val ymax = corners.maxOf { it * up }
        val zmax = corners.maxOf { it * light }

        val center = right * ((xmin + xmax) * 0.5f) +
                up * ((ymin + ymax) * 0.5f) +
                light * ((zmin + zmax) * 0.5f)
        val near = 5f
        val far = near + (zmax - zmin)
        val cameraPos = center - light * (near + (zmax - zmin) * 0.5f)
        val width = xmax - xmin
        val height = ymax - ymin

        val shadowCamera = DefaultCamera(position = cameraPos, direction = light, up = up)
        val shadowProjection = OrthoProjection(
            width = width * 0.51f,
            height = height * 0.51f,
            near = near * 0.98f,
            far = far * 1.05f
        )

        return shadowCamera to shadowProjection
    }

    private fun frustumCorners(
        projection: Projection,
        camera: Camera,
        near: Float,
        far: Float
    ): List<Vec3> {
        projection as FrustumProjection
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
