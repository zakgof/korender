package com.zakgof.korender.baker.editor.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.zakgof.korender.baker.editor.util.sameOrNull
import editor.state.StateHolder
import editor.ui.Theme
import editor.ui.widget.FancyFloatInput

@Composable
fun texturingDialog(holder: StateHolder): () -> Unit {

    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    var show by remember { mutableStateOf(false) }
    val openDialog = { show = true }

    if (show) {
        DialogWindow(
            title = "Texturing setup",
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(Dp.Unspecified, Dp.Unspecified))
        ) {
            val texturings = state.selection.map { model.brushes[it]!! }.flatMap { it.faces }.map { it.texturing }
            val uscale = texturings.map { it.u.scale }.sameOrNull()
            val vscale = texturings.map { it.v.scale }.sameOrNull()
            val uoffset = texturings.map { it.u.offset }.sameOrNull()
            val voffset = texturings.map { it.v.offset }.sameOrNull()
            Column(modifier = Modifier
                .padding(8.dp)
                .background(Theme.background)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("U scale", style = Theme.label)
                    FancyFloatInput(value = uscale, modifier = Modifier.width(36.dp), validator = { it > 0f }) {
                        holder.applyTexturingUScaleToSelection(it)
                    }
                    Text("offset", style = Theme.label)
                    FancyFloatInput(value = uoffset, modifier = Modifier.width(36.dp)) {
                        holder.applyTexturingUOffsetToSelection(it)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("V scale", style = Theme.label)
                    FancyFloatInput(value = vscale, modifier = Modifier.width(36.dp), validator = { it > 0f }) {
                        holder.applyTexturingVScaleToSelection(it)
                    }
                    Text("offset", style = Theme.label)
                    FancyFloatInput(value = voffset, modifier = Modifier.width(36.dp)) {
                        holder.applyTexturingVOffsetToSelection(it)
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
    return openDialog
}