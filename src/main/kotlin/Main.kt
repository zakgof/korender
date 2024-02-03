package com.zakgof.korender

import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.com.zakgof.korender.DefaultCamera
import com.zakgof.korender.lwjgl.LwjglPlatform
import math.Color
import math.Vec3

fun main(): Unit = korender(LwjglPlatform()) {

    val gpuMesh = mesh(3, 3, POS) {
        vertices(0f, 0f, 0f)
        vertices(1f, 0f, 0f)
        vertices(0f, 1f, 0f)
        indices(0, 1, 2)
    }.build(gpu)

    val gpuShader = ShaderBuilder("test.vert", "test.frag").build(gpu)
    val material = MapUniformSupplier(
        "color" to Color(1f, 0f, 0f)
    )

    val renderable = Renderable(gpuMesh, gpuShader, withContext(material))

    camera = DefaultCamera(
        pos = Vec3(0f, 0f, 15f),
        dir = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f)
    )

    projection = OrthoProjection(
        width = 5f,
        height = 5f,
        near = 10f,
        far = 1000f
    )

    add(renderable)
}

