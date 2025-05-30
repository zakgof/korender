package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.Vec3

internal class EnvCaptureContext(
    val resolution: Int,
    val position: Vec3,
    val near: Float,
    val far: Float,
    val insideOut: Boolean,
    val defs: Set<String>,
    val sceneDeclaration: SceneDeclaration
)

internal class FrameCaptureContext(
    val width: Int,
    val height: Int,
    val camera: Camera,
    val projection: Projection,
    val sceneDeclaration: SceneDeclaration
)