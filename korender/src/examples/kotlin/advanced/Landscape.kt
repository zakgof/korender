package advanced

import FlyCamera
import com.zakgof.korender.Bucket
import com.zakgof.korender.Renderable
import com.zakgof.korender.SimpleRenderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.gpu.Gpu
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.*
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.FrustumProjection
import javax.imageio.ImageIO

fun main(): Unit = korender(LwjglPlatform()) {

    light = Vec3(1f, -1f, 1f).normalize()
    val flyCamera = FlyCamera(platform, Vec3(0f, 7f, 384f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    add(terrain(gpu), Bucket.OPAQUE)
    add(sky(gpu), Bucket.SKY)

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
        static("baseTexture", Textures.create("/terrainbase.jpg").build(gpu))

        static("sandTexture", Textures.create("/sand.jpg").build(gpu))
        static("rockTexture", Textures.create("/rock.jpg").build(gpu))
        static("grassTexture", Textures.create("/grass.jpg").build(gpu))
        static("dirtTexture", Textures.create("/dirt.jpg").build(gpu))

        detailScale = 128.0f
    })
    return SimpleRenderable(
        Meshes.heightMap(raster.width - 1, raster.height - 1, 1.0f) { x, y ->
            raster.getPixel(x, y, pix)[0] * 0.0008f
        }.build(gpu),
        material
    )
}

private fun sky(gpu: Gpu): Renderable = SimpleRenderable(
    Meshes.screenQuad().build(gpu),
    Materials.create(
        Shaders.create(gpu, "screen.vert", "cloudsky.frag")
    )
)
