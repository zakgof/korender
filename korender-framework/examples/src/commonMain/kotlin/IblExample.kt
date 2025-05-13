package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.examples.qhull.QuickHull
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    // val loadedMesh = loadMesh(obj("model/tupelo.obj"))



    val freeCamera = FreeCamera(this, ZERO, -1.z)
    OnTouch { freeCamera.touch(it) }


    // val metaball = Metaball(20f, 1.0f) { sqrt(it * 0.05f) * (1f - it * 0.05f) * 10f }
    // val metaball = Metaball(20f, 1.0f) { (it * 0.05f).pow(0.1f) * (1f - it * 0.05f) * 10f }
    val metaball = Metaball(20f, 2.0f) { (it * 0.05f) * (1f - it * 0.05f) * 20f }


    val points = metaball.points
    val hull = QuickHull(points).run()
    val hullMesh = customMesh("hull", hull.points.size, hull.indexes.size, POS, NORMAL) {
        hull.points.forEach { pos(it.pos).normal(it.normal) }
        hull.indexes.forEach { index(it) }
    }

    Frame {
        projection = frustum(3f * width / height, 3f, 3f, 100f)
        AmbientLight(white(0.7f))
        DirectionalLight(Vec3(1.0f, 0.0f, -1.0f), white(4f))
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)

        /*
        CaptureEnv(
            probeName = "radiant", resolution = 128, near = 0.2f, far = 30f, insideOut = true,
            defs = setOf("RADIAL_CAPTURE")
        ) {
            Renderable(
                standart {
                    baseColor = ColorRGBA.Green
                },
                mesh = hullMesh
            )
        }
        CaptureEnv(
            probeName = "normal", resolution = 256, near = 0.2f, far = 30f, insideOut = true,
            defs = setOf("NORMAL_CAPTURE")
        ) {
            metaball.spheres.forEach {
                Renderable(
                    standart {},
                    mesh = sphere(it.r),
                    transform = translate(-hull.center + it.pos)
                )
            }
        }
        CaptureEnv(
            probeName = "albedo", resolution = 256, near = 0.2f, far = 30f, insideOut = true
        ) {
            AmbientLight(white(1f))
            metaball.spheres.forEach {
                Renderable(
                    standart {
                        baseColor = ColorRGBA.Green
                    },
                    mesh = sphere(it.r),
                    transform = translate(-hull.center + it.pos)
                )
            }
        }

        Billboard(
            standart {
                xscale = 3.0f
                yscale = 3.0f
                set("radiantTexture", cubeProbe("radiant"))
                set("colorTexture", cubeProbe("albedo"))
                set("normalTexture", cubeProbe("normal"))
            },
            fragment("mpr/mpr.frag"),
            position = -6.z
        )

        Renderable(
            standart {
                baseColor = ColorRGBA.Red
            },
            mesh = hullMesh,
            transform = scale(0.1f).translate(2.x - 6.z)
        )
         */

        Renderable(
            standart {
                baseColorTexture = texture("model/leaf.png")
                pbr.metallic = 0.0f
                pbr.roughness = 0.9f
            },
            mesh = obj("model/tupelo.obj"),
            transform = scale(0.2f).translate(- 6.z - 2.y)
        )
//
//        metaball.spheres.forEach {
//            Renderable(
//                standart {
//                    baseColor = ColorRGBA.Green
//                },
//                mesh = sphere(it.r / 10f),
//                transform = translate(-2.x - 6.z + it.pos * 0.1f - hull.center * 0.1f)
//            )
//        }

   //     Sky(cubeSky(cubeProbe("albedo")))

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

class Metaball(private val height: Float, private val radius: Float, private val shape: (Float) -> Float) {

    val spheres: List<Sphere>
    val points: List<Vec3>

    init {
        val rnd = Random(1)
        spheres = (0 until 40960).flatMap {
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
            (0..64).map { s.pos + Vec3.random() * s.r }
        }
    }

    class Sphere(val r: Float, val pos: Vec3)
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