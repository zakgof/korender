
import com.zakgof.korender.Renderable
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.*
import com.zakgof.korender.math.*
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.OrthoProjection
import noise.PerlinNoise
import java.awt.image.BufferedImage

fun main(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = Vec3(0f, 4f, 20f), dir = -1.z, up = 1.y)
    projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)

    val material = Materials.standard(gpu,"TRIPLANAR") {
        textureFile = "/sand.png"
        triplanarScale = 0.4f
    }

    val renderables = mutableListOf<Renderable>()
    val range = 0
    for (x in -range..range) {
        for (z in -range..range) {

            val height = 4.5f;//Random.nextInt(3, 10).toFloat()
            val scale = Vec3(0.5f, height, 0.5f)
            val offset = Vec3(x.toFloat(), height * 0.5f, z.toFloat())
            val mesh = Meshes.cube()
             //   .transformPos { it.multpercomp(scale) + offset}
                .build(gpu)
            val renderable = renderable(mesh, material)
            //add(renderable)
            renderables.add(renderable)
        }
    }

    add(renderable(Meshes.sphere(1.1f).build(gpu), material)
        .apply { transform = Transform().translate(Vec3(-2.4f, -2.5f + 4.0f, 10f)) })
    add(renderable(Meshes.sphere(1.1f).build(gpu), material)
        .apply { transform = Transform().translate(Vec3(-2.4f, 2.5f + 4.0f, 10f)) })
    add(renderable(Meshes.sphere(1.1f).build(gpu), material)
        .apply { transform = Transform().translate(Vec3(2.4f, -2.5f + 4.0f, 10f)) })
    add(renderable(Meshes.sphere(1.1f).build(gpu), material)
        .apply { transform = Transform().translate(Vec3(2.4f, 2.5f + 4.0f, 10f)) })

    val shara = renderable(Meshes.sphere(2f).build(gpu), material)
    add(shara)
    renderables.add(shara)

    val cuba = renderable(Meshes.cube(2f).transformPos { it + 4.y }.build(gpu), material)
    add(cuba)
    renderables.add(cuba)

    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    onFrame = { kc ->
        renderables.forEach {
            it.transform = Transform().rotate(1.y, kc.nanoTime * 1e-9f)
        }
        println("FPS=${kc.avgFps} ~FPS=${1e9 / kc.dt}")
    }
}

fun main2(): Unit = korender(LwjglPlatform()) {

    camera = DefaultCamera(pos = 15.z, dir = -1.z, up = 1.y)
    projection = OrthoProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)

    val sphereMesh = Meshes.sphere(4f).build(gpu)
    val gpuShader = ShaderBuilder("test.vert", "test.frag").build(gpu)
    val material = MapUniformSupplier(
        "textureMap" to Textures.create(createNoisyImage()).build(gpu)
    )
    val renderable = renderable(sphereMesh, gpuShader, material)
    add(renderable)

    onResize = {
        projection = OrthoProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    onFrame = {
        renderable.transform = Transform().rotate(1.y, it.nanoTime * 1e-9f)
        println("FPS=${it.avgFps} ~FPS=${1e9 / it.dt}")
    }
}

fun createNoisyImage(): BufferedImage {
    val noise = PerlinNoise()
    val image = Images.create(1024, 1024) { x: Int, y: Int ->
        val n = noise.noise(x.toFloat(), y.toFloat()) * 0.4f + 0.5f
        Color(n, n * 0.6f, n)
        // Color(x.toFloat() / 1024, y.toFloat() / 1024, 0f)
    }
    return image
}

