package com.zakgof.korenderexamples.golden

import com.zakgof.korender.Prefab
import com.zakgof.korender.TerrainMaterial
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korenderexamples.GolderImageCase
import kotlin.math.floor
import kotlin.random.Random

private lateinit var terrain: Prefab<TerrainMaterial>

val allInOne = GolderImageCase(
    title = "All in one",
    init = {
        terrain = clipmapTerrainPrefab("terrain", 0.016f, 10, 6)
    },
    frame = {
        DeferredShading()
        setupLight()
        fireDemo()
        smokeDemo()
        fireballDemo()
        instancedBillboardsDemo()
        objDemo()
        waterDemo()
        terrainDemo()
    }
)

private fun FrameContext.terrainDemo() {
    Prefab(
        terrain {
            colorTexture = texture("terrain/terrain-albedo.jpg", wrap = TextureWrap.ClampToEdge)
            metallicFactor = 0.0f
            heightTexture = texture("terrain/terrain-height.png")
            heightScale = 2.0f
            outsideHeight = -0.04f
            terrainCenter = Vec3(0f, -0.03f, 0f)
        },
        prefab = terrain
    )
}

private fun FrameContext.objDemo() {
    Renderable(
        base {
            colorTexture = texture("model/head.jpg")
            metallicFactor = 0.3f
            roughnessFactor = 0.5f
        },
        mesh = obj("model/head.obj"),
        transform = scale(2.0f).rotate(1.y, -PIdiv2).translate(1.5f.y)
    )
}

private fun FrameContext.floor() =
    Renderable(
        base {
            colorTexture = texture("texture/asphalt-albedo.jpg")
            triplanarScale = 10.0f
            metallicFactor = 0.3f
            roughnessFactor = 0.6f
        },
        mesh = cube(),
        transform = scale(12f, 0.4f, 12f)
    )

private fun FrameContext.setupLight() {
    // DeferredShading()
    AmbientLight(white(0.5f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(1f)) {
//        Cascade(512, 1f, 3f, 0f to 10f, hardwarePcf())
//        Cascade(512, 3f, 7f, 0f to 10f, softwarePcf())
//        Cascade(512, 7f, 20f, 0f to 10f, vsm())
    }
}

private fun FrameContext.fireDemo() = Billboard(
    billboard {
        position = (-5).x + 5.y
        scale = Vec2(2f, 10f)
        effect = fire()
    },
    transparent = true
)

private fun FrameContext.smokeDemo() {
    val n = 100
    for (i in 1..n) {
        val phase = fract(n.toFloat() / i)
        Billboard(
            billboard {
                position = (6.0f + phase * phase * 8f + phase * 2f - 4f).y
                scale = Vec2(5f * phase + 0.5f, 5f * phase + 0.5f)
                effect = smoke(seed = i / n.toFloat(), density = 1.0f - phase)
            },
            transparent = true
        )
    }
}

private fun FrameContext.fireballDemo() {
    Billboard(
        billboard {
            position = 5.x + 3.y
            scale = Vec2(8f, 8f)
            effect = fireball(power = 0.5f)
        },
        transparent = true
    )
}

private fun FrameContext.waterDemo() {
    val sky = fastCloudSky()
    Sky(sky)
    PostProcess(water(transparency = 10.0f, waveScale = 10f, sky = sky))
}

private fun FrameContext.instancedBillboardsDemo() = Billboard(
    billboard {
        color = ColorRGBA.white(0.9f)
        colorTexture = texture("texture/splat.png")
    },
    transparent = true,
    instancing = billboardInstancing(
        id = "particles",
        count = 100,
        dynamic = false
    ) {
        val r = Random(0)
        repeat(100) {
            Instance(
                pos = Vec3(r.nextFloat() * 12f - 6f, r.nextFloat() * 6f, r.nextFloat() * 12f - 6f),
                scale = Vec2(0.4f, 0.4f)
            )
        }
    }
)

private fun fract(time: Float): Float = time - floor(time)
