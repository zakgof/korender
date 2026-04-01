package com.zakgof.korenderexamples.golden

import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korenderexamples.GolderImageCase

val allInOne = GolderImageCase (
    title = "All in one",
    init = {

    },
    frame = {
        DeferredShading()
        DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(3f))
        Renderable(
            base {
                colorTexture = texture("model/head.jpg")
                metallicFactor = 0.3f
                roughnessFactor = 0.5f
            },
            mesh = obj("model/head.obj"),
            transform = scale(7.0f).rotate(1.y, -PIdiv2)
        )
    }
)