import androidx.compose.runtime.Composable
import com.zakgof.insecto.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import procgen.Generator
import procgen.OrbitCamera
import kotlin.math.sqrt
import kotlin.random.Random


fun Int.chance(block: () -> Unit) {
    if (Random.nextInt(100) < this) block()
}

@Composable
@OptIn(ExperimentalResourceApi::class)
fun App() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val tria = tria()
    val orbitCamera = OrbitCamera(this, 400.z, 0.z)

    fun Generator.Triangulation.toCustomMesh(id: String) =
        customMesh(id, this.points.size, this.indexes.size, POS, NORMAL, TEX) {
            pos(*points.toTypedArray())
            normal(*normals.toTypedArray())
            tex(*texs.toTypedArray())
            index(*indexes.toIntArray())
        }

    Frame {

        fun city(texture: String, mesh: Generator.Triangulation) = Renderable(
            standart {
                baseColorTexture = texture(texture)
                pbr.metallic = 0.0f
            },
            mesh = mesh.toCustomMesh("city-$texture"),
            transform = translate(-192f, -32f, -192f)
        )


        AmbientLight(white(0.4f))
        DirectionalLight(Vec3(1f, -1f, -1f).normalize(), white(1.0f))
        DirectionalLight(Vec3(-1f, 1f, 1f).normalize(), white(0.2f))
        PointLight(Vec3(1f, 1f, 5f), white(3.65f))

        OnTouch { orbitCamera.touch(it) }
        camera = orbitCamera.camera(projection, width, height)

        city("lw.jpg", tria.lw())
        city("roof.jpg", tria.rf())

        Renderable(
            standart {
                baseColor = white(0.1f)
                baseColorTexture = texture("roof.jpg")
                pbr.metallic = 0.0f
                pbr.roughness = 1.0f
                triplanarScale = 0.02f
            },
            mesh = cube(),
            transform = scale(800f, 1f, 800f).translate(-32.y)
        )

        Sky(starrySky {
            colorness = 0.2f
            density = 10f
            size = 3f
        })
        Gui {
            Filler()
            Text(id = "fps", fontResource = "ubuntu.ttf", height = 20, text = "FPS ${frameInfo.avgFps}", color = Color(0xFF66FF55))
        }
    }
}

fun tria(): Generator {
    val generator = Generator()
    for (x in 0 until 16) {
        for (z in 0 until 16) {
            val cf = 50 / sqrt(((x - 8) * (x - 8) + (z - 8) * (z - 8) + 1).toDouble()).toInt()
            val h = 36 + cf + Random.nextInt(10)

            if (h > 50) {

                val base = h / 6 + Random.nextInt(3)
                val main = 2 * h / 4 + Random.nextInt(3)
                val loft = h - Random.nextInt(3, 6)

                generator.building(x * 24, z * 24, 16, 16, h) {
                    when (level) {
                        0 -> 40.chance { symcorner(Random.nextInt(1, 3), Random.nextInt(1, 3)) }

                        base -> 30.chance { square(Random.nextInt(1, 3), Random.nextInt(1, 3)) }

                        base -> 30.chance { symcorner(Random.nextInt(1, 3), Random.nextInt(1, 3)) }
                        base + 1 -> 40.chance { symcorner(1, 1) }
                        base + 3 -> 20.chance { symcorner(Random.nextInt(1, 3), Random.nextInt(1, 3)) }

                        main -> square(Random.nextInt(1, 3), Random.nextInt(1, 3))
                        main + 1 -> 40.chance { symcorner(1, 1) }
                        main + 3 -> 40.chance { symcorner(1, 1) }

                        loft -> squareTo(Random.nextInt(5, 8), Random.nextInt(5, 8))
                    }
                }
            }
        }
    }
    return generator
}