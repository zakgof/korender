package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection

internal class SimpleShadower(private val inventory: Inventory, shadowDeclaration: ShadowDeclaration, private val shadowCasters: List<Renderable>) {

    private val frameBuffer: GpuFrameBuffer = inventory.frameBuffer(FrameBufferDeclaration("impl/shadow", shadowDeclaration.mapSize, shadowDeclaration.mapSize, false))
    private val casterShader = inventory.shader(ShaderDeclaration("standard.vert", "standard.frag", setOf("SHADOW_CASTER")))

    val texture = frameBuffer.colorTexture

    fun render(projection: Projection, camera: Camera, light: Vec3): Map<String, Any?> {

        val matrices = updateShadowCamera(projection, camera, light)
        val shadowCamera = matrices.first
        val shadowProjection = matrices.second
        val casterUniforms = mapOf(
            "light" to light,
            "view" to shadowCamera.mat4,
            "projection" to shadowProjection.mat4,
            "cameraPos" to shadowCamera.position
        )

        val uniformDecorator: (UniformSupplier) -> UniformSupplier = {
            UniformSupplier { key ->
                var value = it[key] ?: casterUniforms[key]
                if (value is TextureDeclaration) {
                    value = inventory.texture(value)
                }
                value
            }
        }
        frameBuffer.exec {
            VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
            VGL11.glEnable(VGL11.GL_DEPTH_TEST)
            VGL11.glCullFace(VGL11.GL_BACK)
            shadowCasters.forEach { r ->
                casterShader.render(
                    uniformDecorator(r.uniforms + mapOf("model" to r.transform.mat4())),
                    r.mesh.gpuMesh
                )
            }
        }

        return mapOf(
            "shadowTexture" to frameBuffer.colorTexture,
            "shadowProjection" to shadowProjection.mat4,
            "shadowView" to shadowCamera.mat4
        )
    }

    private fun updateShadowCamera(projection: Projection, camera: Camera, light: Vec3): Pair<DefaultCamera, OrthoProjection> {

        val right = (light % 1.y).normalize()
        val up = (right % light).normalize()
        val corners = frustumCorners(projection, camera)
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
        val far = near + (zmax - zmin) // TODO: this is crazy hack!
        val cameraPos = center - light * (near + (zmax - zmin) * 0.5f)
        val width = xmax - xmin
        val height = ymax - ymin

        val camera = DefaultCamera(position = cameraPos, direction = light, up = up)
        val projection =
            OrthoProjection(width = width * 0.51f, height = height * 0.51f, near = near * 0.98f, far = far * 1.05f)

        return Pair(camera, projection)
    }

    private fun frustumCorners(projection: Projection, camera: Camera): List<Vec3> {
        projection as FrustumProjection
        camera as DefaultCamera

        val shadowFar = projection.near * 10f // TODO

        val up = camera.up * projection.height
        val right = (camera.direction % camera.up).normalize() * projection.width
        val toNear = camera.direction * projection.near
        val toFar = camera.direction * shadowFar
        return listOf(
            camera.position + up + right + toNear,
            camera.position - up + right + toNear,
            camera.position - up - right + toNear,
            camera.position + up - right + toNear,
            camera.position + up + right + toFar,
            camera.position - up + right + toFar,
            camera.position - up - right + toFar,
            camera.position + up - right + toFar,
        )
    }


}
