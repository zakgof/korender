package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.impl.engine.Renderable
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

internal interface Shadower {

    val cascadeNumber: Int

    fun render(
        projection: Projection,
        camera: Camera,
        light: Vec3,
        shadowCasters: List<Renderable>,
        fixer: (Any?) -> Any?
    ): Map<String, Any?>
}