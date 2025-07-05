
import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.sin

@Composable
fun App() = Korender {
    Frame {
        DirectionalLight(Vec3(1f, -1f, -1f))
        Renderable(
            base(
                color = ColorRGBA(0.2f, 1.0f, 0.5f + 0.5f * sin(frameInfo.time), 1.0f),
                metallicFactor = 0.4f
            ),
            mesh = sphere(2.0f),
            transform = translate(sin(frameInfo.time).y)
        )
    }
}