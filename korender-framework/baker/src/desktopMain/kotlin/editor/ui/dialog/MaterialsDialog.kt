package editor.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.editor.ui.widget.MaterialWidget
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.file
import com.zakgof.korender.baker.resources.material
import com.zakgof.korender.baker.resources.pen
import com.zakgof.korender.baker.resources.trash
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.model.Material
import editor.model.Model
import editor.model.TexId
import editor.state.State
import editor.state.StateHolder
import editor.ui.Theme
import editor.ui.toBaseMM
import editor.ui.widget.ColorPicker
import editor.ui.widget.FancyTextInput
import editor.ui.widget.GroupBox
import editor.ui.widget.IconButton
import editor.ui.widget.disabled
import org.jetbrains.compose.resources.painterResource
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun MaterialsDialog(holder: StateHolder): () -> Unit {

    var show by remember { mutableStateOf(false) }
    val openDialog = { show = true }

    if (show) {
        DialogWindow(
            title = "Materials",
            icon = painterResource(Res.drawable.material),
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(800.dp, 500.dp))
        ) {
            val state by holder.state.collectAsState()
            val model by holder.model.collectAsState()
            Row(Modifier.background(Theme.background)) {
                Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                    MaterialSelector(model, state, holder)
                }
                MaterialEditor(holder)
                MaterialPreview(holder)
            }
        }
    }
    return openDialog
}

@Composable
fun ColumnScope.MaterialSelector(model: Model, state: State, holder: StateHolder) =
    Column(Modifier.weight(1f)) {
        var search by remember { mutableStateOf("") }
        val visibleMaterials by remember(model.materials.values, search, state.materialId) {
            derivedStateOf {
                model.materials.values.filter { it.name.contains(search) || it.id == state.materialId }
            }
        }
        GroupBox("Materials") {
            FancyTextInput(
                modifier = Modifier.fillMaxWidth(),
                value = search
            ) { search = it }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .weight(1f)
            ) {
                items(visibleMaterials.size) { i ->
                    val material = visibleMaterials[i]
                    MaterialWidget(material, state.materialId == material.id) {
                        holder.selectMaterial(material)
                    }
                }
            }
        }
    }


@Composable
fun RowScope.MaterialEditor(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    val material = model.materials[state.materialId]!!
    val disabled by remember { derivedStateOf { state.materialId == Material.generic.id } }
    Column(
        Modifier.weight(1f).padding(8.dp)
    ) {
        GroupBox("Operations") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(Res.drawable.file, "New material") {
                    holder.createMaterial()
                }
                if (state.materialId != Material.generic.id) {
                    IconButton(Res.drawable.trash, "Delete Material") {
                        holder.deleteMaterial()
                    }
                }
            }
        }
        Column(
            Modifier.weight(1f)
                .disabled(disabled)
        ) {
            GroupBox("Name") {
                FancyTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    value = model.materials[state.materialId]!!.name,
                    onValueChange = {
                        holder.updateMaterial(material.copy(name = it))
                    }
                )
            }
            GroupBox("Base color") {
                ColorPicker(color = ColorRGBA(material.baseColor), disabled = disabled) {
                    holder.updateMaterial(material.copy(baseColor = it.toLong()))
                }
            }
            if (!disabled) {
                GroupBox("Color Texture") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(material.colorTexture?.path?.let { File(it).name } ?: "-none-", style = Theme.label, modifier = Modifier.weight(1f))
                        IconButton(Res.drawable.pen, "Select texture file") {
                            val file = textureDialog(state, holder)
                            file?.let {
                                holder.updateMaterial(
                                    material.copy(
                                        colorTexture = TexId(file.path),
                                        name = file.name
                                    )
                                )
                            }
                        }
                        material.colorTexture?.let {
                            IconButton(icon = Res.drawable.trash, "Delete texture") {
                                holder.updateMaterial(material.copy(colorTexture = null))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.MaterialPreview(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Box(Modifier.weight(1.6f).fillMaxSize()) {
        Korender({ Res.readBytes(it) }, vSync = true) {
            Frame {
                camera = camera(3.z, -1.z, 1.y)
                projection = projection(width.toFloat() / height.toFloat(), 1f, 1f, 100f)
                AmbientLight(white(0.5f))
                DirectionalLight(Vec3(1f, -1f, -1f), white(0.5f))
                Renderable(
                    model.materials[state.materialId]!!.toBaseMM(false),
                    mesh = cube(),
                    transform = rotate(Quaternion.fromAxisAngle(1.y, frameInfo.time)),
                    transparent = true
                )
            }
        }
    }
}

fun textureDialog(state: State, holder: StateHolder): File? {
    val dialog = FileDialog(Frame(), "Select texture image", FileDialog.LOAD)
    dialog.directory = state.lastTextureDir
    dialog.isVisible = true
    dialog.files.firstOrNull()?.let { holder.setLastTextureDir(it.parent) }
    return dialog.files.firstOrNull()
}