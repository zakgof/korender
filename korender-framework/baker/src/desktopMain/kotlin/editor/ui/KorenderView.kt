package editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.Korender
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import editor.model.Material
import editor.state.State
import editor.state.StateHolder
import editor.ui.TouchHandler.touch
import editor.util.TextureImageCache

object TouchHandler {

    private var down: Vec3? = null

    fun KorenderContext.screenToLook(e: TouchEvent): Vec3 {
        val right = camera.direction.cross(camera.up)

        val nx = (e.x + 0.5f) / width * 2f - 1f
        val ny = 1f - (e.y + 0.5f) / height * 2f

        return (camera.direction * projection.near +
                right * (nx * projection.width * 0.5f) +
                camera.up * (ny * projection.height * 0.5f)
                ).normalize()
    }

    fun KorenderContext.touch(e: TouchEvent, holder: StateHolder) {
        if (e.type == TouchEvent.Type.DOWN) {
            val look = screenToLook(e)
            holder.selectViaRay(look, e.keyboardModifiers.ctrlPressed)
            down = look
        }
        if (e.type == TouchEvent.Type.MOVE && down != null) {
            val newLook = screenToLook(e)
            val rot = Quaternion.shortestArc(newLook, down!!)
            holder.setCamera(camera.position, (rot * camera.direction).normalize(), (rot * camera.up).normalize())
        }
        if (e.type == TouchEvent.Type.UP) {
            down = null
        }
    }
}

@Composable
fun KorenderView(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Korender(appResourceLoader = Res::readBytes, vSync = true) {
        OnKey { keyEvent ->
            if (State.STATE_KEYS.contains(keyEvent.composeKey)) {
                when (keyEvent.type) {
                    KeyEvent.Type.DOWN -> holder.keyDown(keyEvent.composeKey)
                    KeyEvent.Type.UP -> holder.keyUp(keyEvent.composeKey)
                }
            }
        }
        OnTouch { touch(it, holder) }
        Frame {
            AmbientLight(white(0.6f))
            DirectionalLight(Vec3(1f, -1f, -1f), white(1.5f))
            camera = camera(state.camera.position, state.camera.direction, state.camera.up)

            holder.frame(frameInfo.dt)

            model.brushes.values
                .forEach { brush ->
                    brush.faces
                        .forEachIndexed { i, matPlane ->
                            Renderable(
                                model.materials[matPlane.materialId]!!.toBaseMM(
                                    state.selection.contains(brush.id)
                                ),
                                mesh = customMesh(brush.id + "-" + i, 128, 0, POS, NORMAL, TEX, dynamic = true) {
                                    brush.mesh.faces[matPlane.plane]!!.forEach {
                                        pos(*it.points.toTypedArray())
                                        tex(*it.tex.toTypedArray())
                                        normal(it.normal, it.normal, it.normal)
                                    }
                                }
                            )
                        }

                    if (state.selection.contains(brush.id)) {
                        brush.mesh.points.forEach { point ->
                            Renderable(
                                base(color = ColorRGBA.Green),
                                mesh = sphere(state.gridScale * 0.1f),
                                transform = translate(point)
                            )
                        }
                    }
                }
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }
}

context (context: FrameContext)
fun Material.toBaseMM(selected: Boolean): MaterialModifier =
    context.base(
        color = if (selected) ColorRGBA.Red else ColorRGBA(baseColor),
        colorTexture = colorTexture?.let { context.texture(name, TextureImageCache.korender(it.path)) }
    )
