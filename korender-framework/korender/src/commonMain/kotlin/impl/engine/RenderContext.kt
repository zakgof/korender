package com.zakgof.korender.impl.engine

import com.zakgof.korender.Platform
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.context.NodeContext
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


// TODO needs refactoring
internal interface FrameContext {
    val projection: Projection
    val camera: Camera
    val width: Int
    val height: Int
    val time: Float
}

internal class RegularFrameContext(override var width: Int, override var height: Int, val renderContext: RenderContext) : FrameContext {

    private var customProjection: Projection? = null

    override var projection: Projection
        get() = customProjection ?: Projection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f, FrustumProjectionMode)
        set(value) {
            customProjection = value
        }

    override var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)

    override val time
        get() = renderContext.time
}

internal class RenderContext {

    var backgroundColor = ColorRGBA.Transparent

    val frameInfoManager = FrameInfoManager()
    val state = GlState()
    val envProbes = mutableMapOf<String, GlGpuCubeTexture>()
    val frameProbes = mutableMapOf<String, GlGpuTexture>()

    val time
        get() = (Platform.nanoTime() - frameInfoManager.startNanos) * 1e-9f

    var currentRetentionPolicy: RetentionPolicy = TimeRetentionPolicy(10f)
}

internal class CustomFrameContext(
    override val projection: Projection,
    override val camera: Camera,
    val renderContext: RenderContext,
    override val width: Int,
    override val height: Int,
) : FrameContext {

    override val time
        get() = (Platform.nanoTime() - renderContext.frameInfoManager.startNanos) * 1e-9f
}

internal class FrameMaterialModifier(val frameContext: FrameContext, val nodeContext: NodeContext) : InternalMaterialModifier() {

    private val noiseTex = ResourceTextureDeclaration("!noise.png", nodeContext = nodeContext)
    private val fbmTex = ResourceTextureDeclaration("!fbm.png", nodeContext = nodeContext)

    override fun uniform(name: String): UniformGetter<*>? =
        when (name) {
            "noiseTexture" -> TextureGetter<FrameMaterialModifier> { it.noiseTex }
            "fbmTexture" -> TextureGetter<FrameMaterialModifier> { it.fbmTex }
            "view" -> Mat4Getter<FrameMaterialModifier> { it.frameContext.camera.mat4 }
            "projectionWidth" -> FloatGetter<FrameMaterialModifier> { it.frameContext.projection.width }
            "projectionHeight" -> FloatGetter<FrameMaterialModifier> { it.frameContext.projection.height }
            "projectionNear" -> FloatGetter<FrameMaterialModifier> { it.frameContext.projection.near }
            "projectionFar" -> FloatGetter<FrameMaterialModifier> { it.frameContext.projection.far }
            "cameraPos" -> Vec3Getter<FrameMaterialModifier> { it.frameContext.camera.position }
            "cameraDir" -> Vec3Getter<FrameMaterialModifier> { it.frameContext.camera.direction }
            "screenWidth" -> FloatGetter<FrameMaterialModifier> { it.frameContext.width.toFloat() }
            "screenHeight" -> FloatGetter<FrameMaterialModifier> { it.frameContext.height.toFloat() }
            else -> super.uniform(name)
        }
}

