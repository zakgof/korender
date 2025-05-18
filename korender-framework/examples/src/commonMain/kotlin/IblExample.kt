package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.examples.qhull.QHMesh
import com.zakgof.korender.examples.qhull.QuickHull
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class, ExperimentalEncodingApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val freeCamera = FreeCamera(this, 50.z, (-1).z)
    OnTouch { freeCamera.touch(it) }

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

    Frame {
        projection = frustum(3f * width / height, 3f, 3f, 100f)
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)

        AmbientLight(white(0.2f))
        DirectionalLight(Vec3(2.0f, 0.0f, -1.0f), white(3f))

        // renderMeta(metaball, hull, (-20).x)
        // renderTree(leaf, metaball, hull, 20.x)
        // renderHull(hullMesh, 40.x)

        // renderTree(leaf, metaball, hull)
        // return@Frame

//        CaptureEnv(
//            probeName = "radiant", resolution = 128, near = 0.2f, far = 30f, insideOut = true,
//            defs = setOf("RADIANT_CAPTURE")
//        ) {
//            renderHull(hullMesh)
//        }

        CaptureEnv(
            probeName = "radiant-normal", resolution = 128, near = 0.2f, far = 30f, insideOut = true,
            defs = setOf("NORMAL_CAPTURE")
        ) {
            renderHull(hullMesh)
        }
        CaptureEnv(
            probeName = "normal", resolution = 128, near = 0.2f, far = 30f, insideOut = true,
            defs = setOf("NORMAL_CAPTURE")
        ) {
            renderTree(leaf, leafInstances)
        }
        CaptureEnv(
            probeName = "albedo", resolution = 256, near = 0.2f, far = 30f, insideOut = true
        ) {
            AmbientLight(white(1f))
            renderTree(leaf, leafInstances)
        }

        Billboard(
            standart {
                xscale = 40.0f
                yscale = 40.0f
                set("radiantTexture", cubeTexture("holotree/radiant-nx.jpg", "holotree/radiant-ny.jpg", "holotree/radiant-nz.jpg", "holotree/radiant-px.jpg", "holotree/radiant-py.jpg", "holotree/radiant-pz.jpg"))
                set("radiantNormalTexture", cubeProbe("radiant-normal"))
                set("colorTexture", cubeProbe("albedo"))
                set("normalTexture", cubeProbe("normal"))
            },
            fragment("mpr/mpr.frag"),
            position = ZERO
        )

        Sky(cubeSky(cubeProbe("albedo")))

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
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

private fun FrameContext.renderMeta(metaball: Metaball, hull: QHMesh, offset: Vec3) {
    InstancedRenderables(
        standart {
            baseColor = ColorRGBA.Green
        },
        mesh = sphere(1f),
        id = "metaball",
        count = metaball.spheres.size
    ) {
        metaball.spheres.forEach {
            Instance(scale(it.r).translate(offset + (it.pos - hull.center)))
        }
    }
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

class SupportFunctionCalculator(private val kc: KorenderContext, points: List<Vec3>, private val size: Int, private val far: Float) {


    private val hull = QuickHull(points).run()

    fun cubeTexture(): CubeTextureDeclaration {

        val hullIndexes = hull.points.indices.toMutableList()

        val cameras = listOf(
            listOf(-1.x, -1.y),
            listOf(-1.y, -1.z),
            listOf(-1.z, -1.y),
            listOf(1.x, -1.y),
            listOf(1.y, 1.z),
            listOf(1.z, -1.y)
        )
        val faces = cameras.map { renderFace(it[0], it[1], hullIndexes) }
        return kc.cubeTexture("support", faces[0], faces[1], faces[2], faces[3], faces[4], faces[5])

    }

    private fun renderFace(look: Vec3, up: Vec3, hullIndexes: MutableList<Int>): Image {
        val right = (look % up).normalize()
        val image = kc.createImage(size, size, Image.Format.RGBA)
        (0 until size).forEach { x ->
            (0 until size).forEach { y ->
                val dir = (
                        look +
                                right * (-1.0f + 2.0f * (x + 0.5f) / size) +
                                up * (-1.0f + 2.0f * (y + 0.5f) / size)
                        ).normalize()

                hullIndexes.sortBy {
                    val r = hull.points[it].pos
                    if (r * dir > 0f) (dir * (r * dir) - r).lengthSquared() else 1e9f
                }
                val wcnt = 21
                val nearestPoints = (0 until wcnt).map { hullIndexes[it] }.map { hull.points[it] }
                val weights = nearestPoints
                    .map { (dir * (it.pos * dir) - it.pos).length() }
                    .map { exp(-0.4f * it) }
                val norma = 1f / weights.sum()
                val radiant = nearestPoints.indices.sumOf { (nearestPoints[it].pos.length() * weights[it] * norma).toDouble() }.toFloat()
                val normal = nearestPoints.indices.map { nearestPoints[it].normal * weights[it] }.fold(ZERO) { a, i -> a + i }.normalize()

                image.setPixel(
                    x, y, ColorRGBA(
                        (normal.x + 1f) * 0.5f,
                        (normal.y + 1f) * 0.5f,
                        (normal.z + 1f) * 0.5f,
                        radiant / far
                    )
                )
            }
            println("$x")
        }
        return image
    }

}