package editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.Korender
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.baker.editor.util.toKorender
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.context.FrameScope
import com.zakgof.korender.context.KorenderScope
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import editor.model.Material
import editor.state.State
import editor.state.StateHolder
import editor.ui.TouchHandler.touch
import editor.util.TextureImageCache

object TouchHandler {

    private var down: Vec3? = null

    fun KorenderScope.screenToLook(e: TouchEvent): Vec3 {
        val right = camera.direction.cross(camera.up)

        val nx = (e.x + 0.5f) / width * 2f - 1f
        val ny = 1f - (e.y + 0.5f) / height * 2f

        return (camera.direction * projection.near +
                right * (nx * projection.width * 0.5f) +
                camera.up * (ny * projection.height * 0.5f)
                ).normalize()
    }

    fun KorenderScope.touch(e: TouchEvent, holder: StateHolder) {
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
    Korender(resourceLoader = { Res.readBytes("files/$it") }, vSync = true) {
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
            PointLight(20.y, white(4.5f))
            camera = camera(state.camera.position, state.camera.direction, state.camera.up)
            projection = projection(0.1f * width / height, 0.1f, 0.1f, 1e4f)

            holder.frame(frameInfo.dt)

            model.brushes.values
                .filter { brush -> !model.invisibleBrushes.contains(brush.id) }
                .forEach { brush ->
                    brush.mesh.faces.entries
                        .forEachIndexed { i, faceToTris ->
                            val face = faceToTris.key
                            val tris = faceToTris.value
                            val vertexCount = tris.sumOf { it.points.size }
                            Renderable(
                                toBaseMM(
                                    model.materials[face.materialId]!!,
                                    state.selection.contains(brush.id)
                                ),
                                mesh = customMesh(brush.id + "-" + i + "-" + vertexCount, vertexCount, 0, POS, NORMAL, TEX, dynamic = true) {
                                    tris.forEach {tri ->
                                        pos(*tri.points.toTypedArray())
                                        tex(*tri.tex.toTypedArray())
                                        normal(*tri.normals.toTypedArray())
                                    }
                                }
                            )
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

fun FrameScope.toBaseMM(material: Material, selected: Boolean): com.zakgof.korender.Material =
    base {
        color = if (selected) ColorRGBA.Red else material.baseColor.toKorender()
        colorTexture = material.colorTexture?.let { texture(it, TextureImageCache.korender(it)) }
        stochasticSharpness = if (material.stochastic) 12f else null
    }
