package com.zakgof.korender.impl.engine

import com.zakgof.korender.Platform
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection

internal class RenderContext(var width: Int, var height: Int) {

    private var _projection : Projection? = null
    var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
    var projection: Projection
        get() = _projection ?: FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
        set(newProjection) {
            _projection = newProjection
        }
    var lightDirection = Vec3(1f, -1f, 0f).normalize()
    var lightColor = Color(1f, 3f, 3f, 3f)
    var ambientColor = Color(1f, 0.15f, 0.15f, 0.15f)
    var backgroundColor = Color.Black

    val frameInfoManager = FrameInfoManager()

    fun uniforms(): Map<String, Any?> = mapOf(
        "noiseTexture" to ResourceTextureDeclaration("!noise.png"),
        "fbmTexture" to ResourceTextureDeclaration("!fbm.png"),
        "view" to camera.mat4,
        "projection" to projection.mat4,
        "cameraPos" to camera.position,
        "lightDir" to lightDirection,
        "lightColor" to lightColor,
        "ambientColor" to ambientColor,
        "screenWidth" to width.toFloat(),
        "screenHeight" to height.toFloat(),
        "time" to (Platform.nanoTime() - frameInfoManager.startNanos) * 1e-9f
    )

}