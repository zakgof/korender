package com.zakgof.korender

import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.ShaderBuilder
import com.zakgof.korender.projection.OrthoProjection
import math.Color
import math.Vec3
import noise.PerlinNoise
import java.awt.image.BufferedImage

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = Vec3(0f, 0f, 15f), dir = Vec3(0f, 0f, -1f), up = Vec3(0f, 1f, 0f))
    projection = OrthoProjection(width = 5f, height = 5f, near = 10f, far = 1000f)

    val quadMesh = Meshes.quad(4f).build(gpu)
    val gpuShader = ShaderBuilder("test.vert", "test.frag").build(gpu)
    val image = createNoisyImage()
    val gpuTexture = Textures.create(image).build(gpu)
    val material = MapUniformSupplier(
        "textureMap" to gpuTexture
    )
    val renderable = Renderable(quadMesh, gpuShader, withContext(material))
    add(renderable)
}

fun createNoisyImage(): BufferedImage {
    val noise = PerlinNoise()
    val image = Images.create(1024, 1024) { x: Int, y: Int ->
        val n = noise.noise(x.toFloat() , y.toFloat()) * 0.5f + 0.5f
        Color(n, n, n)
    }
    return image
}

