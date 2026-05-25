package editor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakgof.korender.baker.editor.ui.dialog.texturingDialog
import com.zakgof.korender.baker.editor.ui.widget.MaterialWidget
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.adddiamond
import com.zakgof.korender.baker.resources.applymat
import com.zakgof.korender.baker.resources.arrowdown
import com.zakgof.korender.baker.resources.arrowup
import com.zakgof.korender.baker.resources.drag
import com.zakgof.korender.baker.resources.group
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.paint
import com.zakgof.korender.baker.resources.pen
import com.zakgof.korender.baker.resources.plus
import com.zakgof.korender.baker.resources.pointer
import com.zakgof.korender.baker.resources.texsetup
import com.zakgof.korender.baker.resources.ungroup
import com.zakgof.korender.baker.resources.zoomin
import com.zakgof.korender.baker.resources.zoomout
import com.zakgof.korender.math.Vec3
import editor.model.Model
import editor.model.brush.CreatorShape
import editor.state.State
import editor.state.StateHolder
import editor.ui.dialog.EntitiesDialog
import editor.ui.dialog.MaterialsDialog
import editor.ui.widget.EntityWidget
import editor.ui.widget.FancyClickToFloatInput
import editor.ui.widget.FancyClickToTextInput
import editor.ui.widget.GroupBox
import editor.ui.widget.IconButton
import editor.ui.widget.LabeledFloatInput
import editor.ui.widget.RadioButtonRow
import editor.util.nextSane
import editor.util.prevSane
import org.jetbrains.compose.resources.painterResource

@Composable
fun Sidebar(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    val focusManager = LocalFocusManager.current
    Column(
        Modifier.background(Theme.background)
            .width(200.dp)
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
        shape(state, holder)
        grid(holder, state)
        scale(holder, state)
        models(holder, state, model)
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
            IconButton(Res.drawable.zoomout, "Zoom out") { holder.setProjectionScale(state.projectionScale.prevSane()) }
            FancyClickToFloatInput(
                value = state.projectionScale,
                editorModifier = Modifier.width(64.dp),
                validator = { it > 0f }
            ) {
                holder.setProjectionScale(it)
            }
            IconButton(Res.drawable.zoomin, "Zoom in") { holder.setProjectionScale(state.projectionScale.nextSane()) }
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
            IconButton(Res.drawable.minus, "Finer grid") { holder.setGridScale(state.gridScale.prevSane()) }
            FancyClickToFloatInput(
                value = state.gridScale,
                editorModifier = Modifier.width(64.dp),
                validator = { it > 0f }
            ) {
                holder.setGridScale(it)
            }
            IconButton(Res.drawable.plus, "Coarser grid") { holder.setGridScale(state.gridScale.nextSane()) }
        }
    }
}

@Composable
private fun modes(state: State, holder: StateHolder) {
    GroupBox("Mode") {
        RadioButtonRow(
            listOf(
                Res.drawable.pen to "Draw Brushes",
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun shape(state: State, holder: StateHolder) {
    GroupBox("Brush Shape") {

        class ShapeDef(
            val title: String,
            val test: (CreatorShape) -> Boolean,
            val factory: () -> CreatorShape,
            val additionalContent: @Composable () -> Unit = {},
        )

        val options = listOf(
            ShapeDef("Box", { it is CreatorShape.Box }, { CreatorShape.Box }),
            ShapeDef("Right Wedge", { it is CreatorShape.RightWedge }, { CreatorShape.RightWedge }),
            ShapeDef("Symmetric Wedge", { it is CreatorShape.SymmetricWedge }, { CreatorShape.SymmetricWedge }),
            ShapeDef("Cylinder", { it is CreatorShape.Cylinder }, { CreatorShape.Cylinder() }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sides ",
                        style = Theme.label
                    )
                    FancyClickToTextInput(
                        value = "" + (state.creatorShape as CreatorShape.Cylinder).sides,
                        validator = {
                            val int = it.toIntOrNull()
                            int != null && int >= 3 && int <= 64
                        },
                        onValueChange = { holder.setCreatorShape(CreatorShape.Cylinder(it.toInt())) },
                        editorModifier = Modifier.width(32.dp),
                        textModifier = Modifier.width(24.dp),
                    )
                }
            },
            ShapeDef("Cone", { it is CreatorShape.Cone }, { CreatorShape.Cone() }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sides ",
                        style = Theme.label
                    )
                    FancyClickToTextInput(
                        value = "" + (state.creatorShape as CreatorShape.Cone).sides,
                        validator = {
                            val int = it.toIntOrNull()
                            int != null && int >= 3 && int <= 64
                        },
                        onValueChange = { holder.setCreatorShape(CreatorShape.Cone(it.toInt())) },
                        editorModifier = Modifier.width(32.dp),
                        textModifier = Modifier.width(24.dp),
                    )
                }
            },
            ShapeDef("Sphere", { it is CreatorShape.Sphere }, { CreatorShape.Sphere() }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Slices",
                        style = Theme.label
                    )
                    FancyClickToTextInput(
                        value = "" + (state.creatorShape as CreatorShape.Sphere).slices,
                        validator = {
                            val int = it.toIntOrNull()
                            int != null && int >= 3 && int <= 16
                        },
                        onValueChange = { holder.setCreatorShape((state.creatorShape).copy(slices = it.toInt())) },
                        editorModifier = Modifier.width(32.dp),
                        textModifier = Modifier.width(24.dp),
                    )
                    Text(
                        text = "Sectors",
                        style = Theme.label
                    )
                    FancyClickToTextInput(
                        value = "" + (state.creatorShape).sectors,
                        validator = {
                            val int = it.toIntOrNull()
                            int != null && int >= 4 && int <= 16
                        },
                        onValueChange = { holder.setCreatorShape((state.creatorShape).copy(sectors = it.toInt())) },
                        editorModifier = Modifier.width(32.dp),
                        textModifier = Modifier.width(24.dp),
                    )
                }
            }
        )
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            modifier = Modifier.width(200.dp).border(1.dp, Theme.medium, RoundedCornerShape(4.dp)),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .menuAnchor(PrimaryNotEditable)
                    .padding(4.dp)
            ) {
                Text(
                    text = options.first { it.test(state.creatorShape) }.title,
                    style = Theme.label,
                    modifier = Modifier.menuAnchor(PrimaryNotEditable)
                        .weight(1f)
                        .clickable { expanded = true }
                        .padding(1.dp)
                )
                Spacer(Modifier.width(4.dp))
                Image(
                    painterResource(if (expanded) Res.drawable.arrowup else Res.drawable.arrowdown),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }


            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp
            ) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Theme.background,
                    modifier = Modifier.border(1.dp, Theme.dark)
                ) {
                    options.forEach { item ->
                        DropdownMenuItem(
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = MenuDefaults.itemColors(
                                textColor = Theme.light
                            ),
                            onClick = {
                                holder.setCreatorShape(item.factory())
                                expanded = false
                            },
                            text = {
                                Text(
                                    text = item.title,
                                    style = Theme.label
                                )
                            }

                        )
                    }
                }
            }
        }
        options.first { it.test(state.creatorShape) }.additionalContent()
    }
}

@Composable
private fun models(holder: StateHolder, state: State, model: Model) {
    GroupBox("Model") {
        val entitiesDialog = EntitiesDialog(holder)
        state.entityModelId?.let {
            Row(
                modifier = Modifier.height(32.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    EntityWidget(model.entityModels[it]!!, true) {
                        entitiesDialog()
                    }
                }
                IconButton(Res.drawable.adddiamond, "Add to scene") {
                    holder.createEntityInstance()
                }
            }
        } ?: Text(" -- none --", style = Theme.label, modifier = Modifier.fillMaxWidth().clickable {
            entitiesDialog()
        })
    }
}

@Composable
private fun materials(holder: StateHolder, state: State, model: Model) {
    GroupBox("Material") {
        val materialDialog = MaterialsDialog(holder)
        val material = model.materials[state.materialId]!!
        Row(
            modifier = Modifier.height(32.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MaterialWidget(material, false) {
                    materialDialog()
                }
            }
            IconButton(Res.drawable.paint, "Apply to selection") {
                holder.applyMaterialToSelection()
            }
        }

    }
}

@Composable
fun selection(holder: StateHolder, state: State, model: Model) {

    GroupBox("Selection") {
        Column {
            val creator = state.mouseMode == State.MouseMode.CREATOR
            if (state.brushSelection.isEmpty() && state.entityInstanceSelection.isEmpty() || creator) {
                Box(
                    modifier = Modifier.height(32.dp).fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(if (creator) "Brush painter" else "No selection", style = Theme.label)
                }
            } else {
                if (state.entityInstanceSelection.isEmpty()) {
                    brushSelection(holder, state, model)
                } else if (state.brushSelection.isEmpty()) {
                    entitySelection(holder, state, model)
                } else {
                    val count = state.brushSelection.size + state.entityInstanceSelection.size
                    Box(
                        modifier = Modifier.height(32.dp).fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("$count objects", style = Theme.label)
                    }
                }
            }
            selectionDimension(state, model, holder)
        }
    }
}

@Composable
fun brushSelection(holder: StateHolder, state: State, model: Model) {
    val groups = state.brushSelection.mapNotNull { model.brushGroups[it] }.distinct().count()
    val independents = state.brushSelection.count { model.brushGroups[it] == null }
    Row(
        modifier = Modifier.height(32.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
            if (state.brushSelection.size == 1) {
                val brush = model.brushes[state.brushSelection.first()]!!
                FancyClickToTextInput(brush.name) {
                    holder.brushChanged(brush.copy(name = it), true)
                }
            } else if (groups == 1 && independents == 0) {
                val group = model.groups[model.brushGroups[state.brushSelection.first()]!!]!!
                FancyClickToTextInput(group.name) {
                    holder.renameGroup(group.id, it)
                }
            } else if (state.brushSelection.size > 1) {
                Box(
                    modifier = Modifier.height(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${state.brushSelection.size} brushes", style = Theme.label)
                }
            }
        }

        if (groups > 0) {
            IconButton(Res.drawable.ungroup, "Ungroup") {
                holder.ungroupSelection()
            }
        }
        if (independents + groups > 1) {
            IconButton(Res.drawable.group, "Group") {
                holder.groupSelection()
            }
        }
        IconButton(Res.drawable.applymat, "Apply current material") {
            holder.applyMaterialToSelection()
        }
        val texturingDialog = texturingDialog(holder)
        IconButton(Res.drawable.texsetup, "Texture adjustment") {
            texturingDialog()
        }
    }
}

@Composable
fun entitySelection(holder: StateHolder, state: State, model: Model) {
    Box(
        modifier = Modifier.height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state.entityInstanceSelection.size == 1) {
            val entityInstance = model.entityInstances[state.entityInstanceSelection.first()]!!
            FancyClickToTextInput(entityInstance.name) {
                holder.renameEntityInstance(entityInstance, it)
            }
        } else {
            Text("${state.entityInstanceSelection.size} models", style = Theme.label)
        }
    }
}

@Composable
private fun selectionDimension(state: State, model: Model, holder: StateHolder) {
    val bb = if (state.mouseMode == State.MouseMode.CREATOR) state.creator else holder.selectionBB()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            fun setCenter(x: Float, y: Float, z: Float) {
                val newBB = bb!!.move(Vec3(x, y, z))
                state.brushSelection.forEach {
                    holder.brushChanged(model.brushes[it]!!.scale(bb, newBB), true)
                }
            }
            Text("Center", style = Theme.mediumLabel, modifier = Modifier.padding(vertical = 4.dp))
            val coordValidator = { it: Float -> it in -1e6..1e6 }
            LabeledFloatInput("x:", 12.dp, bb?.center?.x, coordValidator) { setCenter(it, bb!!.center.y, bb.center.z) }
            LabeledFloatInput("y:", 12.dp, bb?.center?.y, coordValidator) { setCenter(bb!!.center.x, it, bb.center.z) }
            LabeledFloatInput("z:", 12.dp, bb?.center?.z, coordValidator) { setCenter(bb!!.center.x, bb.center.y, it) }
        }
        Column(
            modifier = Modifier.weight(1.4f)
        ) {
            fun setSize(x: Float, y: Float, z: Float) {
                val newBB = bb!!.resize(Vec3(x, y, z))
                state.brushSelection.forEach {
                    holder.brushChanged(model.brushes[it]!!.scale(bb, newBB), true)
                }
            }
            Text("Dims", style = Theme.mediumLabel, modifier = Modifier.padding(vertical = 4.dp))
            val dimValidator = { it: Float -> it in 1e-3..1e6 }
            LabeledFloatInput("width:", 40.dp, bb?.size?.x, dimValidator) { setSize(it, bb!!.size.y, bb.size.z) }
            LabeledFloatInput("height:", 40.dp, bb?.size?.y, dimValidator) { setSize(bb!!.size.x, it, bb.size.z) }
            LabeledFloatInput("depth:", 40.dp, bb?.size?.z, dimValidator) { setSize(bb!!.size.x, bb.size.y, it) }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun tree(model: Model, state: State, holder: StateHolder) {
    GroupBox("Objects") {
        val scrollState = rememberScrollState()
        Box {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                model.groups.values
                    .forEach { group ->
                        val hidden = model.invisibleBrushes.containsAll(group.brushIds)
                        Text(
                            text = "${group.name} (${group.brushIds.size})",
                            style = if (hidden) Theme.darkLabel else Theme.label,
                            modifier = Modifier
                                .padding(2.dp)
                                .onPointerEvent(PointerEventType.Press) { event ->
                                    holder.selectBrushes(group.brushIds, event.keyboardModifiers.isCtrlPressed, true)
                                },
                            fontWeight = if (state.brushSelection.containsAll(group.brushIds)) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                model.brushes.values
                    .filter { brush -> model.brushGroups[brush.id] == null }
                    .forEach { brush ->
                        val hidden = model.invisibleBrushes.contains(brush.id)
                        Text(
                            text = brush.name,
                            style = if (hidden) Theme.darkLabel else Theme.label,
                            modifier = Modifier
                                .padding(2.dp)
                                .onPointerEvent(PointerEventType.Press) { event ->
                                    holder.selectBrushes(setOf(brush.id), event.keyboardModifiers.isCtrlPressed, true)
                                },
                            fontWeight = if (state.brushSelection.contains(brush.id)) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                model.entityInstances.values
                    .forEach { entityInstance ->
                        Text(
                            text = entityInstance.name,
                            style = Theme.label,
                            modifier = Modifier
                                .padding(2.dp)
                                .onPointerEvent(PointerEventType.Press) { event ->
                                    holder.selectEntityInstance(setOf(entityInstance.id), event.keyboardModifiers.isCtrlPressed)
                                },
                            fontWeight = if (state.entityInstanceSelection.contains(entityInstance.id)) FontWeight.Bold else FontWeight.Normal
                        )
                    }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(6.dp),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
    }
}

