import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.material.MaterialModifiers.options
import com.zakgof.korender.material.MaterialModifiers.standardUniforms
import com.zakgof.korender.material.StandardMaterialOption
import com.zakgof.korender.mesh.Meshes.sphere
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.y
import kotlin.math.sin

@Composable
fun App() = Korender {
    Frame {
        Renderable(
            options(StandardMaterialOption.Color),
            standardUniforms {
                color = Color(1.0f, 0.2f, 1.0f, 0.5f + 0.5f * sin(frameInfo.time))
            },
            mesh = sphere(2.0f),
            transform = Transform().translate(sin(frameInfo.time).y)
        )
    }
}