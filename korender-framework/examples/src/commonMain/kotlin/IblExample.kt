package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.examples.camera.FreeCamera
import com.zakgof.korender.examples.qhull.QuickHull
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IblExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val freeCamera = FreeCamera(this, Vec3.ZERO, -1.z)
    OnTouch { freeCamera.touch(it) }
    projection = frustum(3f * width / height, 3f, 3f, 100f)

//    val points =
//        (0 until 256).map { (-30).x + Vec3.random() * 30.0f } +
//                (0 until 256).map { (30).x + Vec3.random() * 30.0f } +
//                (0 until 256).map { (-60).y + Vec3.random() * 10.0f }


//    val r = Random(1)
//    val points = (0 until 2048).map {
//        val theta = r.nextFloat() * 2f * PI
//        val phi = r.nextFloat() * PI
//        val x = 30f * sin(phi) * cos(theta)
//        val y = 90f * sin(phi) * sin(theta)
//        val z = 20f * cos(phi)
//        Vec3(x, y, z)
//    }

    val metaball = Metaball(20f) { sqrt(it * 0.05f) * (1f - it * 0.05f) * 10f }
    val qhMesh = QuickHull(metaball.points).run()
    val supportCubeTexture = SupportFunctionCalculator(this, qhMesh.points.map { it.pos }, 128, 30f).cubeTexture()

    val hullMesh = customMesh("hull", qhMesh.points.size, qhMesh.indexes.size, POS, NORMAL) {
        qhMesh.points.forEach {
            pos(it.pos)
            normal(it.normal)
        }
        qhMesh.indexes.forEach {
            index(it)
        }
    }

    Frame {
        camera = camera(70.z, -1.z, 1.y)
        scene(metaball, hullMesh)
        return@Frame

//        CaptureEnv(0, 512, Vec3.ZERO, 3f, 100f, insideOut = true) {
//            scene(metaball, hullMesh)
//        }
        camera = freeCamera.camera(projection, width, height, frameInfo.dt)
        Billboard(
            standart {
                baseColorTexture = texture("texture/grass.jpg")
                xscale = 3.0f
                yscale = 3.0f

                set("envDepthTexture0", supportCubeTexture)
                set("envTexture0", supportCubeTexture)
            },
            fragment("mpr/mpr.frag"),
            position = -4f.z
        )

        Sky(cubeSky(supportCubeTexture))

        Gui {
            Column {
                Filler()
                Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
            }
        }
    }
}

private fun FrameContext.scene(metaball: Metaball, hullMesh: MeshDeclaration) {
    AmbientLight(white(0.3f))
    DirectionalLight(Vec3(1.0f, -1.0f, -1.0f), white(2f))
//    metaball.spheres.forEach {
//        Renderable(
//            standart {
//                baseColor = ColorRGBA.Green
//            },
//            mesh = sphere(it.r),
//            transform = translate(it.pos)
//        )
//    }
    Renderable(
        standart {
            baseColor = ColorRGBA.Red
        },
        mesh = hullMesh
    )
}

class Metaball(private val height: Float, private val shape: (Float) -> Float) {

    val spheres: List<Sphere>
    val points: List<Vec3>

    init {
        val rnd = Random(1)
        spheres = (0 until 20480).flatMap {
            val phi = rnd.nextFloat() * 2f * PI
            val h = rnd.nextFloat() * height
            val r = rnd.nextFloat() * height
            if (abs(r - shape(h)) > 0.4f)
                listOf()
            else
                listOf(Vec3(r * sin(phi), h, r * cos(phi)))
        }.map {
            Sphere(2.0f, it)
        }
        points = spheres.flatMap { s ->
            (0..64).map { s.pos + Vec3.random() * s.r }
        }
    }

    class Sphere(val r: Float, val pos: Vec3)
}

class SupportFunctionCalculator(private val kc: KorenderContext, private val points: List<Vec3>, private val size: Int, private val far: Float) {

    fun cubeTexture(): CubeTextureDeclaration {

        val cameras = listOf(
            listOf(-1.x, -1.y),
            listOf(-1.y, -1.z),
            listOf(-1.z, -1.y),
            listOf(1.x, -1.y),
            listOf(1.y, 1.z),
            listOf(1.z, -1.y)
        )
        val faces = cameras.map { renderFace(it[0], it[1]) }
        return kc.cubeTexture("support", faces[0], faces[1], faces[2], faces[3], faces[4], faces[5])

    }

    private fun renderFace(look: Vec3, up: Vec3): Image {
        val right = (look % up).normalize()
        val image = kc.createImage(size, size, Image.Format.Gray)
        (0 until size).forEach { x ->
            (0 until size).forEach { y ->
                val dir = (
                        look +
                                right * (-1.0f + 2.0f * (x + 0.5f) / size) +
                                up * (-1.0f + 2.0f * (y + 0.5f) / size)
                        ).normalize()
                val depth = support(dir)
                image.setPixel(x, y, ColorRGBA(depth / far, 0f, 0f, 0f))
            }
            println("$x")
        }
        return image
    }

    private fun support(look: Vec3): Float = points.maxOf { it * look }.coerceIn(0f, far)
    // private fun support(look: Vec3): Float = 30f

}