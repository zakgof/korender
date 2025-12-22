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
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform.Companion.translate
import com.zakgof.korender.math.Vec3
import editor.model.Material
import editor.state.State
import editor.state.StateHolder
import editor.util.TextureImageCache

@Composable
fun KorenderView(holder: StateHolder, focusHandler: () -> Unit) {
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
        OnTouch {
            if (it.type == TouchEvent.Type.DOWN) {
                focusHandler()
                val right = camera.direction.cross(camera.up)

                val nx = (it.x + 0.5f) / width * 2f - 1f
                val ny = 1f - (it.y + 0.5f) / height * 2f

                val look = (camera.direction * projection.near +
                            right * (nx * projection.width * 0.5f) +
                            camera.up * (ny * projection.height * 0.5f)
                            ).normalize()
                holder.selectViaRay(look, it.keyboardModifiers.ctrlPressed)
            }
        }
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
