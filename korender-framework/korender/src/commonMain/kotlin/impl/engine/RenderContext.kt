package com.zakgof.korender.impl.engine

import com.zakgof.korender.Platform
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.Mat4Getter
import com.zakgof.korender.impl.glgpu.TextureGetter
import com.zakgof.korender.impl.glgpu.UniformGetter
import com.zakgof.korender.impl.glgpu.Vec3Getter
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z


private val noiseTex = ResourceTextureDeclaration("!noise.png", retentionPolicy = ImmediatelyFreeRetentionPolicy)
private val fbmTex = ResourceTextureDeclaration("!fbm.png", retentionPolicy = ImmediatelyFreeRetentionPolicy)

internal val noiseTexGetter = TextureGetter<Any> { noiseTex }
internal val fbmTexGetter = TextureGetter<Any> { fbmTex }

internal class RenderContext(var width: Int, var height: Int) {

    var customProjection: Projection? = null

    var projection: Projection
        get() = customProjection ?: Projection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f, FrustumProjectionMode)
        set(value) {
            customProjection = value
        }

    var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
    var backgroundColor = ColorRGBA.Transparent

    val frameInfoManager = FrameInfoManager()
    val state = GlState()
    val envProbes = mutableMapOf<String, GlGpuCubeTexture>()
    val frameProbes = mutableMapOf<String, GlGpuTexture>()

    val frameMaterialModifier = FrameMaterialModifier()

    inner class FrameMaterialModifier : InternalMaterialModifier() {

        val rc = this@RenderContext

        override fun uniform(name: String): UniformGetter<*>? =
            when (name) {
                "noiseTexture" -> TextureGetter<FrameMaterialModifier> { noiseTex }
                "fbmTexture" -> TextureGetter<FrameMaterialModifier> { fbmTex }
                "view" -> Mat4Getter<FrameMaterialModifier> { it.rc.camera.mat4 }
                "projectionWidth" -> FloatGetter<FrameMaterialModifier> { it.rc.projection.width }
                "projectionHeight" -> FloatGetter<FrameMaterialModifier> { it.rc.projection.height }
                "projectionNear" -> FloatGetter<FrameMaterialModifier> { it.rc.projection.near }
                "projectionFar" -> FloatGetter<FrameMaterialModifier> { it.rc.projection.far }
                "cameraPos" -> Vec3Getter<FrameMaterialModifier> { it.rc.camera.position }
                "cameraDir" -> Vec3Getter<FrameMaterialModifier> { it.rc.camera.direction }
                "screenWidth" -> FloatGetter<FrameMaterialModifier> { it.rc.width.toFloat() }
                "screenHeight" -> FloatGetter<FrameMaterialModifier> { it.rc.height.toFloat() }
                "time" -> FloatGetter<FrameMaterialModifier> { (Platform.nanoTime() - frameInfoManager.startNanos) * 1e-9f }
                else -> super.uniform(name)
            }
    }

    fun defaultTarget() = FrameTarget(width, height, "colorTexture", "depthTexture")
}

