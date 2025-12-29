package editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.FloatMath.PI
import com.zakgof.korender.math.FloatMath.PIdiv2
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import editor.state.StateHolder

@Composable
fun KorenderView(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Korender({ Res.readBytes(it) }) {


        Frame {
            AmbientLight(white(0.2f))
            DirectionalLight(Vec3(1f, -1f, 1f), white(0.5f))
            camera = camera(state.camera.position, state.camera.direction, state.camera.up)

            holder.frame(frameInfo.dt)

            model.brushes.forEach { brush ->
                Renderable(
                    base(),
                    mesh = quad(brush.max.x - brush.center.x, brush.max.y - brush.center.y),
                    transform = translate(Vec3(brush.center.x, brush.center.y, brush.max.z))
                )
                Renderable(
                    base(),
                    mesh = quad(brush.max.x - brush.center.x, brush.max.y - brush.center.y),
                    transform =
                        rotate(1.y, PI).
                        translate(Vec3(brush.center.x, brush.center.y, brush.min.z))
                )
                Renderable(
                    base(),
                    mesh = quad(brush.max.z - brush.center.z, brush.max.y - brush.center.y),
                    transform =
                        rotate(1.y, -PIdiv2).
                        translate(Vec3(brush.min.x, brush.center.y, brush.center.z))
                )
                Renderable(
                    base(),
                    mesh = quad(brush.max.z - brush.center.z, brush.max.y - brush.center.y),
                    transform =
                        rotate(1.y, PIdiv2).
                        translate(Vec3(brush.max.x, brush.center.y, brush.center.z))
                )
                Renderable(
                    base(),
                    mesh = quad(brush.max.x - brush.center.x, brush.max.z - brush.center.z),
                    transform =
                        rotate(1.x, PIdiv2).
                        translate(Vec3(brush.center.x, brush.min.y, brush.center.z))
                )
                Renderable(
                    base(),
                    mesh = quad(brush.max.x - brush.center.x, brush.max.z - brush.center.z),
                    transform =
                        rotate(1.x, -PIdiv2).
                        translate(Vec3(brush.center.x, brush.max.y, brush.center.z))
                )
            }

        }
    }
}