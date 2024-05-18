package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.declaration.TextureDeclaration
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.engine.CascadeDeclaration
import com.zakgof.korender.impl.engine.CustomShaderDeclaration
import com.zakgof.korender.impl.engine.FrameBufferDeclaration
import com.zakgof.korender.impl.engine.Inventory
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.impl.gpu.GpuFrameBuffer
import com.zakgof.korender.impl.material.MapUniformSupplier
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection

internal class SingleShadower(private val index: Int, private val inventory: Inventory, private val decl: CascadeDeclaration) : Shadower {

    override val cascadeNumber = 1
    private val frameBuffer: GpuFrameBuffer = inventory.frameBuffer(FrameBufferDeclaration("shadow$index", decl.mapSize, decl.mapSize, false))

    override fun render(projection: Projection, camera: Camera, light: Vec3, shadowCasters: List<Renderable>): UniformSupplier {

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
                // TODO: need to copy all the defs from the original shader
                val casterShader = inventory.shader(CustomShaderDeclaration("standard.vert", "standard.frag", setOf("SHADOW_CASTER")))
                casterShader.render(
                    uniformDecorator(r.uniforms + mapOf("model" to r.transform.mat4())),
                    r.mesh.gpuMesh
                )
            }
        }

        return MapUniformSupplier(
            "shadowTexture$index" to frameBuffer.colorTexture,
            "shadowProjection$index" to shadowProjection.mat4,
            "shadowView$index" to shadowCamera.mat4
        )
    }

    private fun updateShadowCamera(projection: Projection, camera: Camera, light: Vec3): Pair<DefaultCamera, OrthoProjection> {

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
        val shadowProjection = OrthoProjection(width = width * 0.51f, height = height * 0.51f, near = near * 0.98f, far = far * 1.05f)

        return shadowCamera to shadowProjection
    }

    private fun frustumCorners(projection: Projection, camera: Camera, near: Float, far: Float): List<Vec3> {
        projection as FrustumProjection
        camera as DefaultCamera
        val up = camera.up * projection.height
        val right = (camera.direction % camera.up).normalize() * projection.width
        val toNear = camera.direction * near
        val toFar = camera.direction * far
        val upFar = up * (far / near)
        val rightFar = right * (far / near)
        return listOf(
            camera.position + up + right + toNear,
            camera.position - up + right + toNear,
            camera.position - up - right + toNear,
            camera.position + up - right + toNear,
            camera.position + upFar + rightFar + toFar,
            camera.position - upFar + rightFar + toFar,
            camera.position - upFar - rightFar + toFar,
            camera.position + upFar - rightFar + toFar,
        )
    }


}
