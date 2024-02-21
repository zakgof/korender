import com.zakgof.korender.*
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.lwjgl.LwjglPlatform
import com.zakgof.korender.material.Images
import com.zakgof.korender.material.Materials
import com.zakgof.korender.material.ShaderBuilder
import com.zakgof.korender.math.*
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.projection.FrustumProjection
import de.javagl.obj.ObjReader
import javax.imageio.ImageIO
import kotlin.random.Random

fun main(): Unit = korender(LwjglPlatform()) {

    light = Vec3(1f, -1f, 1f).normalize()
    val flyCamera = FlyCamera(platform, Vec3(0f, 2f, 256f))
    onResize = {
        projection = FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
    }

    add(terrain(gpu), Bucket.OPAQUE)

    val cat = cat(gpu)
    shadower.add(cat)
    add(cat, Bucket.OPAQUE)

    add(sky(gpu), Bucket.SKY)

    val farter = Farter(this)
    add(farter.renderable, Bucket.TRANSPARENT)

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        farter.update(camera.position(), frameInfo.dt * 1e-9f)
        println("FPS=~${frameInfo.avgFps}")
    }
}

private fun terrain(gpu: Gpu): Renderable {
    val raster = ImageIO.read(Images.javaClass.getResourceAsStream("/heightmap.png")).raster
    val pix = FloatArray(3)
    return Renderable(
        Meshes.heightMap(raster.width - 1, raster.height - 1, 1.0f) { x, y ->
            raster.getPixel(x, y, pix)[0] * 0.1f
        }.build(gpu),
        Materials.standard(gpu, "TRIPLANAR", "APERIODIC", "SHADOW_RECEIVER") {
            colorFile = "/grass-aperiodic.png"
            aperiodicFile = "/aperiodic.png"
            triplanarScale = 0.3f
        }
    )
}

private fun cat(gpu: Gpu): Renderable = Renderable(
    Meshes.create(ObjReader.read(Images.javaClass.getResourceAsStream("/cat-red.obj"))).build(gpu),
    Materials.standard(gpu) {
        colorFile = "/cat-red.jpg"
        ambient = 1.0f
        diffuse = 1.0f
        specular = 0.0f
    }).apply {
    transform = Transform().scale(0.1f)
        .rotate(1.x, -PI * 0.5f)
        .rotate(1.y, PI * 0.5f)
        .translate(Vec3(0f, 0f, 230f))
}

private fun sky(gpu: Gpu): Renderable = Renderable(
    Meshes.screenQuad().build(gpu),
    Materials.create(
        ShaderBuilder("screen.vert", "starsky.frag").build(gpu)
    )
)

class Farticle {
    var ttl = Random.nextFloat() * 5.0f
    var pos = Vec3(-1f, 2f, 230f)
    private var v = Vec3.random() - 4.x
    fun update(dt: Float) {
        pos += v * dt
        ttl -= dt
        if (ttl < 0) {
            ttl = Random.nextFloat() * 5.0f
            pos = Vec3(-1f, 2f, 230f)
            v = Vec3.random() - 4.x
        }
    }
}

class Farter(kc: KorenderContext) {

    private val particleNum = 1000
    private val mesh = Meshes.billboard().instancing(particleNum).build(kc.gpu)
    private val particles = Array(particleNum) { Farticle() }
    val renderable = Renderable(mesh, Materials.billboard(kc.gpu) { colorFile = "/splat.png" })

    fun update(campos: Vec3, secs: Float) {
        particles.forEach { it.update(secs) }
        particles.sortByDescending { (it.pos - campos).lengthSquared() }
        for (i in particles.indices) {
            val particle = particles[i]
            for (v in 0 until 4) {
                mesh.updateVertex(i * 4 + v) {
                    it.pos = particle.pos
                    val scale = (5.0f - particle.ttl) * 0.05f
                    it.scale = Vec2(scale, scale)
                }
            }
        }
        mesh.updateGpu()
    }

}