package com.zakgof.korender.baker.editor.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.zakgof.korender.Korender
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.baker.editor.ui.widget.EntityWidget
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.file
import com.zakgof.korender.baker.resources.material
import com.zakgof.korender.baker.resources.plus
import com.zakgof.korender.baker.resources.trash
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.model.Model
import editor.model.entity.EntityModel
import editor.state.State
import editor.state.StateHolder
import editor.ui.Theme
import editor.ui.dialog.fileDialog
import editor.ui.widget.FancyClickToFloatInput
import editor.ui.widget.FancyClickToTextInput
import editor.ui.widget.FancyTextInput
import editor.ui.widget.GroupBox
import editor.ui.widget.IconButton
import editor.util.BoundingSphere
import org.jetbrains.compose.resources.painterResource
import java.io.File

@Composable
fun EntitiesDialog(holder: StateHolder): () -> Unit {

    var show by remember { mutableStateOf(false) }
    val openDialog = { show = true }

    if (show) {
        DialogWindow(
            title = "Models",
            icon = painterResource(Res.drawable.material), // TODO
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(800.dp, 630.dp))
        ) {
            val state by holder.state.collectAsState()
            val model by holder.model.collectAsState()
            val focusManager = LocalFocusManager.current
            Row(
                Modifier.background(Theme.background)
                    .focusable()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }) {
                Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                    EntitySelector(model, state, holder)
                }
                EntityEditor(holder)
                EntityPreview(holder)
            }
        }
    }
    return openDialog
}

@Composable
fun ColumnScope.EntitySelector(model: Model, state: State, holder: StateHolder) =
    Column(Modifier.weight(1f)) {
        var search by remember { mutableStateOf("") }
        val visibleEntities by remember(model.entityModels.values, search, state.entityModelId) {
            derivedStateOf {
                model.entityModels.values.filter { it.name.contains(search) || it.id == state.entityModelId }
            }
        }
        GroupBox("Models") {
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
                items(visibleEntities.size) { i ->
                    val entityModel = visibleEntities[i]
                    EntityWidget(entityModel, state.entityModelId == entityModel.id) {
                        holder.selectEntityModel(entityModel)
                    }
                }
            }
        }
    }


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RowScope.EntityEditor(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    val entityModel = model.entityModels[state.entityModelId]
    Column(
        Modifier.weight(1f).padding(8.dp)
    ) {
        GroupBox("Operations") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(Res.drawable.file, "New Model") {
                    modelFileDialog(state, holder) {
                        val name = it.nameWithoutExtension
                        val filename = it.absolutePath
                        val entityModel = EntityModel(name, filename)
                        holder.addEntityModel(entityModel)
                    }
                }
                if (state.entityModelId != null) {
                    IconButton(Res.drawable.trash, "Delete Model") {
                        holder.deleteMaterial()
                    }
                    IconButton(Res.drawable.plus, "Insert into Scene") {
                        holder.createEntityInstance()
                    }
                }
            }
        }
        state.entityModelId?.let {
            Column(
                Modifier.weight(1f)
            ) {
                GroupBox("Name") {
                    FancyClickToTextInput(
                        textModifier = Modifier.fillMaxWidth(),
                        editorModifier = Modifier.fillMaxWidth(),
                        value = model.entityModels[state.entityModelId]!!.name,
                        onValueChange = {
                            holder.updateEntityModelName(entityModel!!, it)
                        }
                    )
                }

                GroupBox("Properties") {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Default scale", style = Theme.label, modifier = Modifier.weight(1f))
                        FancyClickToFloatInput(value = entityModel!!.defaultScale, validator = { it in 1e-3f..1e3f }) {
                            holder.updateEntityModelScale(entityModel, it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.EntityPreview(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    var transform by remember(state.entityModelId) { mutableStateOf(Transform.IDENTITY) }
    Box(Modifier.weight(1.6f).fillMaxSize()) {
        Korender(
            resourceLoader = {
                File(it).readBytes()
            }, vSync = true
        ) {
            Frame {
                state.entityModelId?.let {
                    AmbientLight(white(0.6f))
                    val entityModel = model.entityModels[state.entityModelId]!!
                    camera = camera(-5.z, 1.z, 1.y)
                    projection = projection(width.toFloat() / height.toFloat(), 1f, 1f, 300f)
                    AmbientLight(white(0.5f))
                    DirectionalLight(Vec3(1f, -1f, -1f), white(0.5f))
                    Model(entityModel.filename, transform = transform, onUpdate = { objInfo ->
                        if (entityModel.points.isEmpty()) {
                            val points = collectModelPoints(objInfo)
                            entityModel.points += points
                            val bs = BoundingSphere.fromPoints(points)
                            if (bs.radius > 0f) {
                                transform = Transform.translate(-bs.center).scale(1f / bs.radius)
                            }
                        }
                    })
                }
            }
        }
    }
}

fun modelFileDialog(state: State, holder: StateHolder, handler: (File) -> Unit) =
    fileDialog(
        "Select 3d model file", false, state.persistentState.lastDir, "3D model files",
        listOf("obj", "gltf", "glb", "kr")
    ) {
        handler(it)
        holder.setLastTextureDir(it.parentFile)
    }

fun collectModelPoints(modelInfo: ModelInfo): List<Vec3> {
    fun collect(node: ModelInfo.Node, parent: Mat4 = Mat4.IDENTITY): List<Vec3> {
        val transform = parent * (node.transform?.mat4 ?: Mat4.IDENTITY)
        return (node.mesh?.vertices?.mapNotNull { it.pos?.let { pt -> transform * pt } } ?: emptyList()) +
                (node.children?.flatMap { collect(it, transform) } ?: emptyList())
    }
    return modelInfo.instances.flatMap(::collect)
}
