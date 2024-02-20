
import com.zakgof.korender.Bucket
import com.zakgof.korender.KorenderContext
import com.zakgof.korender.Renderable
import com.zakgof.korender.geometry.Meshes
import com.zakgof.korender.korender
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

    addTerrain()
    addCat()
    addSky()
    val farter = Farter(this)

    onFrame = { frameInfo ->
        camera = flyCamera.idle(frameInfo.dt)
        farter.update(camera.position(),frameInfo.dt * 1e-9f)
        println("FPS=~${frameInfo.avgFps}")
    }
}

private fun KorenderContext.addTerrain() {
    val raster = ImageIO.read(Images.javaClass.getResourceAsStream("/heightmap.png")).raster
    val pix = FloatArray(3)
    add(Renderable(
        Meshes.heightMap(raster.width - 1, raster.height - 1, 1.0f) { x, y ->
            raster.getPixel(x, y, pix)[0] * 0.1f
        }.build(gpu),
        Materials.standard(gpu, "TRIPLANAR", "APERIODIC", "SHADOW_RECEIVER") {
            colorFile = "/grass-aperiodic.png"
            aperiodicFile = "/aperiodic.png"
            triplanarScale = 0.3f
        }
    ))
}

private fun KorenderContext.addCat() {
    val cat = Renderable(
        Meshes.create(ObjReader.read(Images.javaClass.getResourceAsStream("/cat-red.obj"))).build(gpu),
        Materials.standard(gpu) {
            colorFile = "/cat-red.jpg"
            ambient = 1.0f
            diffuse = 1.0f
            specular = 0.0f
        })
    add(cat)
    shadower.add(cat)
    cat.transform = Transform().scale(0.1f)
        .rotate(1.x, -PI * 0.5f)
        .rotate(1.y, PI * 0.5f)
        .translate(Vec3(0f, 0f, 230f))
}

private fun KorenderContext.addSky() {
    add(
        Renderable(
            Meshes.screenQuad().build(gpu),
            Materials.create(
                ShaderBuilder("screen.vert", "starsky.frag").build(gpu)
            )
        ), Bucket.SKY
    )
}

class Farticle {
    var ttl = Random.nextFloat() * 5.0f
    var pos = Vec3(-1f, 2f, 230f)
    private var v = Vec3.random() - 4.x
    fun update(dt: Float): Boolean {
        pos += v * dt
        ttl -= dt
        return ttl > 0
    }
}

class Farter(kc: KorenderContext) {

    private val particleNum = 1000
    private val mesh = Meshes.billboard().instancing(particleNum).build(kc.gpu)
    private val particles = Array(particleNum) { Farticle() }

    init {
        kc.add(Renderable(mesh, Materials.billboard(kc.gpu, "/splat.png")), Bucket.TRANSPARENT)
    }

    fun update(campos: Vec3, secs: Float) {
        for (i in particles.indices) {
            if (!particles[i].update(secs))
                particles[i] = Farticle()
        }
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
        mesh.update()
    }

}