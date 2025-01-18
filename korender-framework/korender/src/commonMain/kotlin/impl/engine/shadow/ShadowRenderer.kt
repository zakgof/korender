package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.RenderContext
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glDisable
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_CULL_FACE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.OrthoProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.ceil
import kotlin.math.round

internal object ShadowRenderer {

    fun render(
        id: String,
        inventory: Inventory,
        lightDirection: Vec3,
        declarations: List<CascadeDeclaration>,
        index: Int,
        renderContext: RenderContext,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): ShadowerData? {
        val declaration = declarations[index]
        val frameBuffer = inventory.frameBuffer(
            FrameBufferDeclaration(
                "shadow-$id",
                declaration.mapSize,
                declaration.mapSize,
                listOf(GlGpuTexture.Preset.VSM),
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
            glClearColor(0f, 0f, 0f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glEnable(GL_DEPTH_TEST)
            glCullFace(GL_BACK)
            glDisable(GL_CULL_FACE)
            shadowCasters.forEach { casterRenderable ->
                casterRenderable.render(casterUniforms, fixer)
            }
            glEnable(GL_CULL_FACE)
        }

        return ShadowerData(
            frameBuffer.colorTextures[0],
            Mat4(
                0.5f, 0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.0f, 0.5f,
                0.0f, 0.0f, 0.5f, 0.5f,
                0.0f, 0.0f, 0.0f, 1.0f
            ) * shadowProjection.mat4 * shadowCamera.mat4,
            listOf(
                if (index == 0) 0f else declaration.near,
                if (index == 0) 0f else declarations[index-1].far,
                if (index == declarations.size-1) 1e10f else declarations[index+1].near,
                if (index == declarations.size-1) 1e10f else declaration.far
            )
        )
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
        val corners = frustumCorners(projection , camera, declaration.near, declaration.far)
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

        val near = 1f // TODO
        val volume = 150f // TODO

        val fragSize = dim / declaration.mapSize * 2.0f // TODO ?
        val depthSize = volume / 255f

        val moveUpSnap = round((ymin + ymax) * 0.5f / fragSize) * fragSize
        val moveRightSnap = round((xmin + xmax) * 0.5f / fragSize) * fragSize
        val depthSnap = ceil(zmax / depthSize) * depthSize

        val centerBottom = right * moveRightSnap +
                up * moveUpSnap +
                light * depthSnap


        val far = near + volume
        val cameraPos = centerBottom - light * far

        // println("SHADOW CAMERA: $cameraPos")

        // TODO wrong matrix, divide by 2
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
    val cascade: List<Float>
)
