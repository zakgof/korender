package com.zakgof.korender

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.glgpu.GlGpuFrameBuffer
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.ShaderBuilder
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.OrthoProjection
import com.zakgof.korender.projection.Projection
import gl.VGL11
import org.lwjgl.opengl.GL11

class Shadower(private val gpu: Gpu) {

    private val casterShader: GpuShader = ShaderBuilder("test.vert", "test.frag", "SHADOW_CASTER").build(gpu)
    private val casters = mutableListOf<Renderable>()
    private val fb: GlGpuFrameBuffer = GlGpuFrameBuffer(1024, 1024, false)

    var camera: Camera? = null
    var projection: Projection? = null

    fun add(renderable: Renderable) = casters.add(renderable)

    fun render(light: Vec3) {
        if (casters.isEmpty()) {
            return
        }
        updateCamera(light)
        fb.exec {
            VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            VGL11.glEnable(GL11.GL_DEPTH_TEST)
            VGL11.glCullFace(VGL11.GL_FRONT);
            casters.forEach() { r ->
                casterShader.render({
                    r.uniforms[it] ?: mapOf(
                        "model" to r.transform.mat4(),
                        "view" to camera!!.mat4(),
                        "projection" to projection!!.mat4(),
                        "cameraPos" to camera!!.position()
                    )[it]
                }, r.mesh.gpuMesh)
            }
        }
    }

    private fun updateCamera(light: Vec3) {
        val left = (1.y % light).normalize()
        val up = (light % left).normalize()
        val corners = casters.mapNotNull { it.worldBoundingBox }
            .flatMap { it.corners }
        val xmin = corners.minOf { it * left }
        val ymin = corners.minOf { it * up }
        val zmin = corners.minOf { it * light }
        val xmax = corners.maxOf { it * left }
        val ymax = corners.maxOf { it * up }
        val zmax = corners.maxOf { it * light }

        val center = Vec3((xmin + xmax) * 0.5f, (ymin + ymax) * 0.5f, (zmin + zmax) * 0.5f)
        val near = 5f
        val far = near + (zmax - zmin)
        val cameraPos = center - light * (near + (zmax - zmin) * 0.5f)
        val width = xmax - xmin
        val height = ymax - ymin

        camera = DefaultCamera(pos = cameraPos, dir = light, up = up)
        projection =
            OrthoProjection(width = width * 0.51f, height = height * 0.51f, near = near * 0.9f, far = far * 1.1f)
    }

    fun texture() = fb.colorTexture

}
