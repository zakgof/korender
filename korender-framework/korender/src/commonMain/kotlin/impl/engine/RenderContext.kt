package com.zakgof.korender.impl.engine

import com.zakgof.korender.Platform
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuShadowTextureList
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.GlGpuTextureList
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.LogProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

internal class RenderContext(var width: Int, var height: Int) {

    var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
    var projection = Projection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f, FrustumProjectionMode)
    var backgroundColor = ColorRGBA.Transparent

    val frameInfoManager = FrameInfoManager()
    val state = GlState()
    val envProbes = mutableMapOf<String, GlGpuCubeTexture>()
    val frameProbes = mutableMapOf<String, GlGpuTexture>()

    fun frameUniforms(m: MutableMap<String, Any?>) {
        m["noiseTexture"] = ResourceTextureDeclaration("!noise.png", retentionPolicy = ImmediatelyFreeRetentionPolicy)
        m["fbmTexture"] = ResourceTextureDeclaration("!fbm.png", retentionPolicy = ImmediatelyFreeRetentionPolicy)
        m["view"] = camera.mat4
        m["projectionWidth"] = projection.width
        m["projectionHeight"] = projection.height
        m["projectionNear"] = projection.near
        m["projectionFar"] = projection.far
        m["cameraPos"] = camera.position
        m["cameraDir"] = camera.direction
        m["screenWidth"] = width.toFloat()
        m["screenHeight"] = height.toFloat()
        m["time"] = (Platform.nanoTime() - frameInfoManager.startNanos) * 1e-9f
    }

    fun contextUniforms(m: MutableMap<String, Any?>) {
        m["noiseTexture"] = ResourceTextureDeclaration("!noise.png", retentionPolicy = ImmediatelyFreeRetentionPolicy)
        m["fbmTexture"] = ResourceTextureDeclaration("!fbm.png", retentionPolicy = ImmediatelyFreeRetentionPolicy)
        m["shadowTextures[0]"] = GlGpuTextureList(List(5) { null }, 5)
        m["pcfTextures[0]"] = GlGpuShadowTextureList(List(5) { null }, 5)
    }

    fun contextPlugins(): Map<String, String> {
        val plugins = mutableMapOf("vprojection" to projection.mode.plugin())
        if (projection.mode is LogProjectionMode) {
            plugins["depth"] = "!shader/plugin/depth.log.frag"
        }
        return plugins
    }

    private fun ProjectionMode.plugin() = when (this) {
        is FrustumProjectionMode -> "!shader/plugin/vprojection.frustum.vert"
        is OrthoProjectionMode -> "!shader/plugin/vprojection.ortho.vert"
        is LogProjectionMode -> "!shader/plugin/vprojection.log.vert"
        else -> ""
    }

}

