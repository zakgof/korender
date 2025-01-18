
import androidx.compose.runtime.Composable
import city.Generator
import city.OrbitCamera
import city.Triangulation
import city.roads
import com.zakgof.insecto.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Color.Companion.Blue
import com.zakgof.korender.math.Color.Companion.Green
import com.zakgof.korender.math.Color.Companion.Red
import com.zakgof.korender.math.Color.Companion.White
import com.zakgof.korender.math.Color.Companion.white
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.sqrt
import kotlin.random.Random


fun Int.chance(block: () -> Unit) {
    if (Random.nextInt(100) < this) block()
}

@Composable
@OptIn(ExperimentalResourceApi::class)
fun App() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val tria = tria()
    val roads = roads()
    val orbitCamera = OrbitCamera(this, -112.z + 1.y + 3.x, -102.z + 1.y + 3.x)

    fun Triangulation.toCustomMesh(id: String) =
        customMesh(id, this.points.size, this.indexes.size, POS, NORMAL, TEX) {
            pos(*points.toTypedArray())
            normal(*normals.toTypedArray())
            tex(*texs.toTypedArray())
            index(*indexes.toIntArray())
        }

    val windowsMesh = tria.lw().toCustomMesh("windows")
    val roofMesh = tria.rf().toCustomMesh("roof")
    val crossroadsMesh = roads.crossroads.toCustomMesh("cross")
    val roadsMesh = roads.roads.toCustomMesh("roadz")

    Frame {

        OnTouch { orbitCamera.touch(it) }
        camera = orbitCamera.camera(projection, width, height)
        projection = frustum(width = 0.3f * width / height, height = 0.3f, near = 1f, far = 500f)

        AmbientLight(white(0.1f))
        DirectionalLight(Vec3(2f, -4f, 0f).normalize(), white(3.0f)) {
            Cascade(1024, (projection as FrustumProjectionDeclaration).near, 11f)
            //Cascade(1024, 9f, 80f)
        }

        for (xx in 0..4) {
            for (zz in 0..4) {
                PointLight(Vec3(-192f + 4 + 96f * xx, 8f, -192f + 4 + 96f * zz), white(1.5f))
                Renderable(
                    standart { baseColor = Red },
                    mesh = sphere(0.4f),
                    transform = translate(Vec3(-192f + 4 + 96f * xx, 8f, -192f + 4 + 96f * zz))
                )
            }
        }

        Renderable(
            standart {
                baseColor = White
                baseColorTexture = texture("city/dw.jpg")
                set("windowTexture", texture("city/lw.jpg"))
                pbr.metallic = 0.6f
            },
            plugin("texture", "city/window.texture.plugin.frag"),
            mesh = windowsMesh
        )

        Renderable(
            standart {
                baseColorTexture = texture("city/roof.jpg")
                pbr.metallic = 0.6f
            },
            mesh = roofMesh,
        )

        Renderable(
            standart {
                baseColor = white(0.4f)
                pbr.metallic = 0.0f
                pbr.roughness = 0.4f
            },
            mesh = cube(),
            transform = scale(800f, 1f, 800f).translate(-0.55f.y)
        )

        Renderable(
            standart {
                baseColor = White
                baseColorTexture = texture("city/crossroad.jpg")
                pbr.metallic = 0.0f
                pbr.roughness = 0.8f
            },
            mesh = crossroadsMesh
        )

        Renderable(
            standart {
                baseColor = White
                baseColorTexture = texture("city/road.jpg")
                pbr.metallic = 0.0f
                pbr.roughness = 0.8f
            },
            mesh = roadsMesh
        )

        Renderable(standart { baseColor = Blue }, mesh = sphere(0.3f), transform = translate(-82.z + 0.x + 3.y))
        Renderable(standart { baseColor = Green }, mesh = sphere(0.3f), transform = translate(-82.z + 8.x + 3.y))

        Scene(gltfResource = "city/swat.glb", transform = scale(0.005f).translate(-102.z + 3.x))

        Sky(starrySky {
            colorness = 0.2f
            density = 40f
            size = 3f
        })

        // Filter(fragment("city/shadow-debug.frag"))


        Gui {
            Filler()
            Text(id = "fps", fontResource = "ubuntu.ttf", height = 20, text = "FPS ${frameInfo.avgFps.toInt()}", color = Color(0xFF66FF55))
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

                generator.building(x * 24 - 192 + 8, z * 24 - 192 + 8, 16, 16, h) {
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