package advanced

import FlyCamera
import com.zakgof.korender.*
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Images
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.Shaders
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection
import javax.imageio.ImageIO

fun main(): Unit = korender(LwjglPlatform()) {

    light = Vec3(0f, -0.1f, 1f).normalize()
    val flyCamera = FlyCamera(platform, Vec3(0f, 7f, 384f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    add(terrain(gpu), Bucket.OPAQUE)
    add(Skies.fancyClouds(gpu), Bucket.SKY)
    addFilter(Filter(gpu, "postwater.frag"))
    addFilter(Filter(gpu, "atmosphere.frag"))

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        println("FPS=~${frameInfo.avgFps}")
    }
}

private fun terrain(gpu: Gpu): Renderable {
    val raster = ImageIO.read(Images.javaClass.getResourceAsStream("/heightmap.png")).raster
    val pix = FloatArray(3)

    val shader = Shaders.create(gpu, "standard.vert", "terrain.frag")
    val material = Materials.create(shader, StockUniforms(gpu).apply {
        staticTex("baseTexture", "/terrainbase.jpg")
        staticTex("sandTexture", "/sand.jpg")
        staticTex("rockTexture", "/rock.jpg")
        staticTex("grassTexture", "/grass.jpg")
        staticTex("dirtTexture", "/dirt.jpg")
        detailScale = 128.0f
    })
    return SimpleRenderable(
        Meshes.heightMap(raster.width - 1, raster.height - 1, 1.0f) { x, y ->
            raster.getPixel(x, y, pix)[0] * 0.0008f
        }.build(gpu),
        material,
        true
    )
}
