package com.zakgof.korender.baker

import androidx.compose.runtime.Composable
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.Korender
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

@Composable
@OptIn(ExperimentalResourceApi::class, ExperimentalEncodingApi::class)
fun Baker() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val basePath = "D:\\kot\\dev\\assets\\"

    // val metaball = Metaball(20f, 1.0f) { sqrt(it * 0.05f) * (1f - it * 0.05f) * 10f }
    val metaball = Metaball(20f, 3.0f, 8000, 48) { (it * 0.05f).pow(0.1f) * (1f - it * 0.05f) * 10f }
    // val metaball = Metaball(20f, 4.0f, 4096, 32) { (it * 0.05f) * (1f - it * 0.05f) * 20f }


    val hull = QuickHull(metaball.points.map { it.pos }).run()
    val hullMesh = customMesh("hull", hull.points.size, hull.indexes.size, POS, NORMAL) {
        hull.points.forEach { pos(it.pos).normal(it.normal) }
        hull.indexes.forEach { index(it) }
    }
    val leaf = customMesh("leaf", 4, 6, POS, NORMAL, TEX) {
        pos(Vec3(-0.5f, -0.5f, 0f)).normal(1.z).tex(0f, 0f)
        pos(Vec3(0.5f, -0.5f, 0f)).normal(1.z).tex(1f, 0f)
        pos(Vec3(0.5f, 0.5f, 0f)).normal(1.z).tex(1f, 1f)
        pos(Vec3(-0.5f, 0.5f, 0f)).normal(1.z).tex(0f, 1f)

        index(0, 1, 2, 0, 2, 3)
    }
    val leafInstances = metaball.points.mapIndexed { index, pt ->
        val quaternion = (0..640)
            .map { index * 1023 + it }
            .map { Quaternion.fromAxisAngle(Vec3.random(it), Random(index * 777 + it).nextFloat() * 100f) }
            .maxBy { (it * 1.z) * pt.n }

        rotate(quaternion)
            .scale(0.6f)
            .translate((pt.pos - hull.center))
    }

    val radiantImages = captureEnv(resolution = 128, near = 0.2f, far = 30f, insideOut = true, defs = setOf("RADIANT_CAPTURE")) {
        renderHull(hullMesh)
    }
    saveCubeMap(radiantImages, basePath + "radiant-")

    val radiantNormalImages = captureEnv(resolution = 128, near = 0.2f, far = 30f, insideOut = true, defs = setOf("NORMAL_CAPTURE")) {
        renderHull(hullMesh)
    }
    saveCubeMap(radiantNormalImages, basePath + "radiant-normal-")

    val normalImages = captureEnv(resolution = 128, near = 0.2f, far = 30f, insideOut = true, defs = setOf("NORMAL_CAPTURE")) {
        renderTree(leaf, leafInstances)
    }
    saveCubeMap(normalImages, basePath + "normal-")

    val albedoImages = captureEnv(resolution = 256, near = 0.2f, far = 30f, insideOut = true) {
        AmbientLight(white(1f))
        renderTree(leaf, leafInstances)
    }
    saveCubeMap(albedoImages, basePath + "albedo-")

    Frame {
        projection = frustum(3f * width / height, 3f, 3f, 100f)
        camera = camera(20.z, -1.z, 1.y)

        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2.0f, 0.0f, -1.0f), white(3f))
        Billboard(
            standart {
                xscale = 10.0f
                yscale = 10.0f
                set("radiantTexture", cubeTexture("radiant", radiantImages))
                set("radiantNormalTexture", cubeTexture("radiant-normal", radiantNormalImages))
                set("colorTexture", cubeTexture("albedo", albedoImages))
                set("normalTexture", cubeTexture("normal", normalImages))
            },
            fragment("!shader/effect/radial.frag"),
            position = ZERO
        )
    }
}

fun saveCubeMap(images: CubeTextureImages, pathPrefix: String) {
    images.entries.map {
        val img = it.value
        val bytes = img.toRaw()
        val bi = BufferedImage(img.width, img.height, TYPE_INT_RGB)
        val raster = bi.raster
        val pixel = IntArray(3)
        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                pixel[0] = bytes[(x + y * img.width) * 4].toUByte().toInt()
                pixel[1] = bytes[(x + y * img.width) * 4 + 1].toUByte().toInt()
                pixel[2] = bytes[(x + y * img.width) * 4 + 2].toUByte().toInt()
                raster.setPixel(x, y, pixel)
            }
        }
        ImageIO.write(bi, "jpg", File("${pathPrefix}${it.key.toString().lowercase()}.jpg"))
    }
}

private fun FrameContext.renderHull(hullMesh: MeshDeclaration, offset: Vec3 = ZERO) {
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
        },
        mesh = hullMesh,
        transform = translate(offset)
    )
}

private fun FrameContext.renderTree(leaf: MeshDeclaration, leafInstances: List<Transform>) {
    InstancedRenderables(
        standart {
            baseColorTexture = texture("model/leaf.png")
        },
        mesh = leaf,
        id = "metapoints",
        transparent = true,
        count = leafInstances.size,
        static = true
    ) {
        leafInstances.forEach { Instance(it) }
    }
}

class Metaball(
    private val height: Float,
    private val radius: Float,
    private val sphereCount: Int = 4096,
    private val pointCount: Int = 64,
    private val shape: (Float) -> Float
) {

    val spheres: List<Sphere>
    val points: List<Pt>

    init {
        val rnd = Random(1)
        spheres = (0 until sphereCount).flatMap {
            val phi = rnd.nextFloat() * 2f * PI
            val h = rnd.nextFloat() * height
            val r = rnd.nextFloat() * height
            if (abs(r - shape(h)) > 0.6f * radius)
                listOf()
            else
                listOf(Vec3(r * sin(phi), h, r * cos(phi)))
        }.map {
            Sphere(radius, it)
        }
        points = spheres.flatMap { s ->
            (0 until pointCount).map {
                val n = Vec3.random()
                Pt(s.pos + n * s.r, n)
            }
        }
    }

    class Sphere(val r: Float, val pos: Vec3)
    class Pt(val pos: Vec3, val n: Vec3)
}