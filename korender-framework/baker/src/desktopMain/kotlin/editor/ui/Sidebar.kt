package editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakgof.korender.baker.editor.ui.dialog.texturingDialog
import com.zakgof.korender.baker.editor.ui.widget.MaterialWidget
import com.zakgof.korender.baker.editor.util.sameOrNull
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.applymat
import com.zakgof.korender.baker.resources.drag
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.newmaterial
import com.zakgof.korender.baker.resources.pen
import com.zakgof.korender.baker.resources.plus
import com.zakgof.korender.baker.resources.pointer
import com.zakgof.korender.baker.resources.texsetup
import com.zakgof.korender.baker.resources.zoomin
import com.zakgof.korender.baker.resources.zoomout
import editor.model.Material
import editor.model.Model
import editor.model.TexId
import editor.state.State
import editor.state.StateHolder
import editor.ui.dialog.MaterialsDialog
import editor.ui.dialog.textureDialog
import editor.ui.widget.FancyClickToFloatInput
import editor.ui.widget.FancyClickToTextInput
import editor.ui.widget.GroupBox
import editor.ui.widget.IconButton
import editor.ui.widget.RadioButtonRow

@Composable
fun Sidebar(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    val focusManager = LocalFocusManager.current
    Column(
        Modifier.background(Theme.background)
            .width(160.dp)
            .fillMaxHeight()
            .focusable()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
            .padding(2.dp)
    ) {
        modes(state, holder)
        grid(holder, state)
        scale(holder, state)
        materials(holder, state, model)
        selection(holder, state, model)
        tree(model, state, holder)
    }
}

@Composable
private fun scale(holder: StateHolder, state: State) {
    GroupBox("Scale") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(Res.drawable.zoomout, "-") { holder.setProjectionScale(state.projectionScale / 2f) }
            FancyClickToFloatInput(
                value = state.projectionScale,
                editorModifier = Modifier.width(64.dp),
                validator = { it > 0f }
            ) {
                holder.setProjectionScale(it)
            }
            IconButton(Res.drawable.zoomin, "+") { holder.setProjectionScale(state.projectionScale * 2f) }
        }
    }
}

@Composable
private fun grid(holder: StateHolder, state: State) {
    GroupBox("Grid") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(Res.drawable.minus, "-") { holder.setGridScale(state.gridScale / 2f) }
            FancyClickToFloatInput(
                value = state.gridScale,
                editorModifier = Modifier.width(64.dp),
                validator = { it > 0f }
            ) {
                holder.setGridScale(it)
            }
            IconButton(Res.drawable.plus, "+") { holder.setGridScale(state.gridScale * 2f) }
        }
    }
}

@Composable
private fun modes(state: State, holder: StateHolder) {
    GroupBox("Mode") {
        RadioButtonRow(
            listOf(
                Res.drawable.pen to "Draw Objects",
                Res.drawable.pointer to "Select Objects",
                Res.drawable.drag to "Drag Grid"
            ),
            state.mouseMode.ordinal,
            Modifier.fillMaxWidth()
        ) {
            holder.setMouseMode(State.MouseMode.entries.toTypedArray()[it])
        }
    }
}

@Composable
private fun materials(holder: StateHolder, state: State, model: Model) {
    GroupBox("Material") {
        val materialDialog = MaterialsDialog(holder)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(Res.drawable.pen, "Edit Materials") { materialDialog() }
            IconButton(Res.drawable.newmaterial, "New textured Material") {
                textureDialog(state, holder)?.let { file ->
                    val material = Material(file.name, TexId(file.path))
                    holder.addMaterial(material)
                }
            }
        }
        val material = model.materials[state.materialId]!!
        MaterialWidget(material, false) {
            materialDialog()
        }
    }
}

@Composable
fun selection(holder: StateHolder, state: State, model: Model) {

    GroupBox("Selection") {
        Column(modifier = Modifier.height(80.dp)) {
            if (state.selection.isEmpty()) {
                Text("No selection", style = Theme.label)
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    IconButton(Res.drawable.applymat, "Apply current material") {
                        holder.applyMaterialToSelection()
                    }
                    val texturingDialog = texturingDialog(holder)
                    IconButton(Res.drawable.texsetup, "Texture adjustment") {
                        texturingDialog()
                    }
                }
                if (state.selection.size > 1) {
                    Text("${state.selection.size} objects", style = Theme.label)
                } else if (state.selection.size == 1) {
                    val brush = model.brushes[state.selection.first()]!!
                    FancyClickToTextInput(brush.name) {
                        holder.brushChanged(brush.copy(name = it))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val texturings = state.selection.map { model.brushes[it]!! }.flatMap { it.faces }.map { it.texturing }
                    val worldScale = texturings.map { it.worldScale }.sameOrNull()
                    Text("World scale", style = Theme.label, modifier = Modifier.weight(1f))
                    TriStateCheckbox(
                        state = when (worldScale) {
                            null -> ToggleableState.Indeterminate
                            true -> ToggleableState.On
                            false -> ToggleableState.Off
                        }, onClick = {
                            holder.applyTexturingWorldScaleToSelection(worldScale?.not() ?: true)
                        })
                }
            }
        }
    }
}

@Composable
fun tree(model: Model, state: State, holder: StateHolder) {
    GroupBox("Objects") {
        Column {
            model.brushes.values.forEach { brush ->
                Text(
                    text = brush.name,
                    style = Theme.label,
                    modifier = Modifier
                        .padding(2.dp)
                        .clickable {
                            holder.setSelection(setOf(brush.id))
                        },
                    fontWeight = if (state.selection.contains(brush.id)) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

