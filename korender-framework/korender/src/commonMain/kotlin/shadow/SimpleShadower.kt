package com.zakgof.korender.shadow

import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.gl.VGL11
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.gpu.GpuFrameBuffer
import com.zakgof.korender.gpu.GpuShader
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

class SimpleShadower(gpu: Gpu, size: Int = 1024) : Shadower {

    private val casterShader: GpuShader = Shaders.standard(gpu, "SHADOW_CASTER")
    private val casters = mutableListOf<Renderable>()
    private val fb: GpuFrameBuffer = gpu.createFrameBuffer(size, size, false)

    override val texture = fb.colorTexture
    override var camera: Camera? = null
    override var projection: Projection? = null

    override fun add(renderable: Renderable) = casters.add(renderable)

    override fun render(light: Vec3) {
        if (casters.isEmpty()) {
            return
        }
        updateCamera(light)
        fb.exec {
            VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
            VGL11.glEnable(VGL11.GL_DEPTH_TEST)
            VGL11.glCullFace(VGL11.GL_BACK)
            casters.forEach { r ->
                casterShader.render({
                    r.uniforms[it] ?: mapOf(
                        "light" to light,
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
//        val right = (light % 1.y).normalize()
//        val up = (right % light).normalize()
//        val corners = casters.mapNotNull { it.worldBoundingBox }
//            .flatMap { it.corners }
//        val xmin = corners.minOf { it * right }
//        val ymin = corners.minOf { it * up }
//        val zmin = corners.minOf { it * light }
//        val xmax = corners.maxOf { it * right}
//        val ymax = corners.maxOf { it * up }
//        val zmax = corners.maxOf { it * light }
//
//        val center = right * ((xmin + xmax) * 0.5f) +
//                    up *  ((ymin + ymax) * 0.5f) +
//                    light * ((zmin + zmax) * 0.5f)
//        val near = 5f
//        val far = near + (zmax - zmin) + 30.0f // TODO: this is crazy hack!
//        val cameraPos = center - light * (near + (zmax - zmin) * 0.5f)
//        val width = xmax - xmin
//        val height = ymax - ymin
//
//        camera = DefaultCamera(pos = cameraPos, dir = light, up = up)
//        projection =
//            OrthoProjection(width = width * 0.51f, height = height * 0.51f, near = near * 0.98f, far = far * 1.05f)
    }

}
