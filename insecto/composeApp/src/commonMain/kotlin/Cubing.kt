
import androidx.compose.runtime.Composable
import com.zakgof.insecto.Res
import com.zakgof.korender.Attributes.NORMAL
import com.zakgof.korender.Attributes.POS
import com.zakgof.korender.Attributes.TEX
import com.zakgof.korender.Korender
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.z
import org.jetbrains.compose.resources.ExperimentalResourceApi
import procgen.Generator
import procgen.OrbitCamera

@Composable
@OptIn(ExperimentalResourceApi::class)
fun App() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val tria = tria()
    val orbitCamera = OrbitCamera(this, 20.z, 0.z)
    Frame {
        OnTouch { orbitCamera.touch(it) }
        camera = orbitCamera.camera(projection, width, height)
        Renderable(
            standart {
                pbr.metallic = 0.1f
            },
            mesh = customMesh("cubez", tria.points.size, tria.indexes.size, POS, NORMAL, TEX) {
                tria.points.forEach { pos(it) }
                tria.normals.forEach { normal(it) }
                tria.texs.forEach { tex(it) }
                index(*tria.indexes.toIntArray())
            },
            transform = translate(-8f, -8f, 0f).scale(0.2f)
        )
    }
}

fun tria() = Generator(16, 16, 16).run {
    when (level) {
        1 -> {
            corner(-4, -4)
            corner(12, 12)
            corner(-4, 12)
            corner(12, -4)
        }

        8 -> {
            flatx(-4)
            flatx(12)
            flaty(-4)
            flaty(12)
        }
    }
}