package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.OrthoProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

internal object ShadowRenderer {

    fun render(
        id: String,
        inventory: Inventory,
        lightDirection: Vec3,
        declaration: CascadeDeclaration,
        renderContext: RenderContext,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): ShadowerData? {
        val frameBuffer = inventory.frameBuffer(
            FrameBufferDeclaration(
                "shadow-$id",
                declaration.mapSize,
                declaration.mapSize,
                1, // TODO: depth only
                false
            )
        ) ?: return null

        val matrices = updateShadowCamera(renderContext.projection, renderContext.camera, lightDirection, declaration)
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
                casterRenderable.render(casterUniforms, fixer)
            }
        }

        return ShadowerData(
            frameBuffer.colorTextures[0], // TODO OR DEPTH ?
            Mat4(
                0.5f, 0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.0f, 0.5f,
                0.0f, 0.0f, 0.5f, 0.5f,
                0.0f, 0.0f, 0.0f, 1.0f
            )  * shadowProjection.mat4 * shadowCamera.mat4
        )
    }

    private fun updateShadowCamera(
        projection: Projection,
        camera: Camera,
        light: Vec3,
        declaration: CascadeDeclaration
    ): Pair<DefaultCamera, OrthoProjection> {

        val right = (light % 1.y).normalize()
        val up = (right % light).normalize()
        val corners = frustumCorners(projection, camera, declaration.near, declaration.far)
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

internal class ShadowerData (
    val texture: GlGpuTexture,
    val bsp: Mat4
)
