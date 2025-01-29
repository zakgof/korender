package com.zakgof.korender.impl.engine

import com.zakgof.korender.Platform
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

internal class RenderContext(var width: Int, var height: Int) {

    private var _projection: Projection? = null
    var camera: Camera = DefaultCamera(20.z, -1.z, 1.y)
    var projection: Projection
        get() = _projection ?: FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
        set(newProjection) {
            _projection = newProjection
        }
    var backgroundColor = Color.Black

    val frameInfoManager = FrameInfoManager()

    fun uniforms(m: MutableMap<String, Any?>) {
        m["noiseTexture"] = ResourceTextureDeclaration("!noise.png")
        m["fbmTexture"] = ResourceTextureDeclaration("!fbm.png")
        m["view"] = camera.mat4
        m["projection"] = projection.mat4
        m["cameraPos"] = camera.position
        m["cameraDir"] = camera.direction
        m["screenWidth"] = width.toFloat()
        m["screenHeight"] = height.toFloat()
        m["time"] = (Platform.nanoTime() - frameInfoManager.startNanos) * 1e-9f
    }

}