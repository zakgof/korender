package editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuBarScope
import editor.ui.dialog.EntitiesDialog
import com.zakgof.korender.baker.editor.ui.dialog.texturingDialog
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.applymat
import com.zakgof.korender.baker.resources.center
import com.zakgof.korender.baker.resources.copy
import com.zakgof.korender.baker.resources.cube
import com.zakgof.korender.baker.resources.cubeplus
import com.zakgof.korender.baker.resources.cut
import com.zakgof.korender.baker.resources.export
import com.zakgof.korender.baker.resources.eye
import com.zakgof.korender.baker.resources.file
import com.zakgof.korender.baker.resources.group
import com.zakgof.korender.baker.resources.load
import com.zakgof.korender.baker.resources.material
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.newmaterial
import com.zakgof.korender.baker.resources.noeye
import com.zakgof.korender.baker.resources.paste
import com.zakgof.korender.baker.resources.play
import com.zakgof.korender.baker.resources.plus
import com.zakgof.korender.baker.resources.save
import com.zakgof.korender.baker.resources.texsetup
import com.zakgof.korender.baker.resources.trash
import com.zakgof.korender.baker.resources.ungroup
import com.zakgof.korender.baker.resources.zoomin
import com.zakgof.korender.baker.resources.zoomout
import editor.model.Material
import editor.state.State
import editor.state.StateHolder
import editor.ui.dialog.MaterialsDialog
import editor.ui.dialog.confirmDialog
import editor.ui.dialog.fileDialog
import editor.ui.dialog.okDialog
import editor.ui.dialog.textureDialog
import editor.util.nextSane
import editor.util.prevSane
import editor.walk.walkerDialog
import org.jetbrains.compose.resources.painterResource
import java.io.File

@Composable
fun FrameWindowScope.Menu(holder: StateHolder) =
    MenuBar {
        val state by holder.state.collectAsState()
        file(holder)
        view(holder)
        edit(holder)
        materials(holder)
        models(holder)
        if (state.brushSelection.isNotEmpty()) {
            selection(holder)
        }
    }

@Composable
private fun MenuBarScope.file(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    val modified = state.lastSavedModelHash != System.identityHashCode(model)
    Menu("File") {
        val newProjectConfirmDialog = confirmDialog("New project", "Discard changes and start a new project ?") {
            holder.newProject()
        }
        Item("New", icon = painterResource(Res.drawable.file)) {
            if (modified) newProjectConfirmDialog() else holder.newProject()
        }

        fun load() {
            fileDialog("Open Project", false, state.persistentState.lastDir, "Korender maps", listOf("krmap")) {
                holder.loadProject(it)
            }
        }

        val loadProjectConfirmDialog = confirmDialog("Load Project", "Discard changes and load a project ?") {
            load()
        }
        Item("Open Project", painterResource(Res.drawable.load), shortcut = KeyShortcut(Key.O, ctrl = true)) {
            if (modified) loadProjectConfirmDialog() else load()
        }

        state.savePath?.let {
            Item("Save Project", painterResource(Res.drawable.save), shortcut = KeyShortcut(Key.S, ctrl = true)) {
                holder.saveProject(File(state.savePath!!))
            }
        }
        Item("Save Project as...", painterResource(Res.drawable.save)) {
            fileDialog("Save Project", true, state.persistentState.lastDir, "Korender maps", listOf("krmap")) {
                holder.saveProject(it)
            }
        }
        Separator()
        
        val recentProjects = state.persistentState.recentProjects
        if (recentProjects.isNotEmpty()) {
            recentProjects.forEach { projectPath ->
                val projectFile = File(projectPath)
                val projectName = projectFile.nameWithoutExtension
                val loadProjectConfirmDialog = confirmDialog("Load Project", "Discard changes and load a project ?") {
                    holder.loadProject(projectFile)
                }
                Item(projectName) {
                    if (modified) loadProjectConfirmDialog() else holder.loadProject(projectFile)
                }
            }
            Separator()
        }
        
        val walkDialog = walkerDialog()
        Item("Dry-Run", painterResource(Res.drawable.play)) {
            walkDialog(holder.dryRun())
        }
        Item("Export Scene", painterResource(Res.drawable.export)) {
            fileDialog("Export Scene", true, state.persistentState.lastDir,"Korender model files", listOf("kr")) {
                holder.compileToFile(it.path)
            }
        }
    }
}

@Composable
private fun MenuBarScope.view(holder: StateHolder) {
    val state by holder.state.collectAsState()
    Menu("View") {
        Item("Coarser grid", painterResource(Res.drawable.plus), shortcut = KeyShortcut(Key.LeftBracket, ctrl = true)) {
            holder.setGridScale(state.gridScale.nextSane())
        }
        Item("Finer grid", painterResource(Res.drawable.minus), shortcut = KeyShortcut(Key.RightBracket, ctrl = true)) {
            holder.setGridScale(state.gridScale.prevSane())
        }
        Separator()
        Item("Zoom in", painterResource(Res.drawable.zoomin), shortcut = KeyShortcut(Key.Plus, ctrl = true)) {
            holder.setProjectionScale(state.projectionScale.nextSane())
        }
        Item("Zoom out", painterResource(Res.drawable.zoomout), shortcut = KeyShortcut(Key.Minus, ctrl = true)) {
            holder.setProjectionScale(state.projectionScale.prevSane())
        }
        Separator()
        Item("Center on selection", painterResource(Res.drawable.center), shortcut = KeyShortcut(Key.E, ctrl = true)) {
            holder.zoomOnSelection()
        }
        Item("Reset Views", painterResource(Res.drawable.eye)) { // TODO icon
            holder.resetViews()
        }
    }
}

@Composable
private fun MenuBarScope.edit(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Menu("Edit") {
        RadioButtonItem("Draw Objects", selected = (state.mouseMode == State.MouseMode.CREATOR)) {
            holder.setMouseMode(State.MouseMode.CREATOR)
        }
        RadioButtonItem("Select Objects", selected = (state.mouseMode == State.MouseMode.SELECT)) {
            holder.setMouseMode(State.MouseMode.SELECT)
        }
        RadioButtonItem("Drag Grid", selected = (state.mouseMode == State.MouseMode.DRAG)) {
            holder.setMouseMode(State.MouseMode.DRAG)
        }
        Separator()
        Item("Copy", painterResource(Res.drawable.copy), shortcut = KeyShortcut(Key.C, ctrl = true)) {
            holder.copy()
        }
        Item("Cut", painterResource(Res.drawable.cut), shortcut = KeyShortcut(Key.X, ctrl = true)) {
            holder.cut()
        }
        Item("Paste", painterResource(Res.drawable.paste), enabled = (state.brushSelection.isNotEmpty() || state.entityInstanceSelection.isNotEmpty()), shortcut = KeyShortcut(Key.V, ctrl = true)) {
            holder.paste()
        }
        Item("Select All", enabled = (model.brushes.isNotEmpty()), shortcut = KeyShortcut(Key.A, ctrl = true)) {
            holder.selectAll()
        }
        val deleteDialog = confirmDialog("Delete", "Delete selected objects ?") { holder.deleteSelected() }
        Item("Delete", painterResource(Res.drawable.trash), shortcut = KeyShortcut(Key.Delete)) {
            deleteDialog()
        }
        Separator()
        // TODO icon
        Item("Undo", painterResource(Res.drawable.plus), enabled = holder.canUndo(), shortcut = KeyShortcut(Key.Z, ctrl = true)) {
            holder.undo()
        }
        // TODO icon
        Item("Redo", painterResource(Res.drawable.minus), enabled = holder.canRedo(), shortcut = KeyShortcut(Key.Y, ctrl = true)) {
            holder.redo()
        }
    }
}

@Composable
private fun MenuBarScope.materials(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val materialDialog = MaterialsDialog(holder)
    Menu("Materials") {
        Item("Materials Library", icon = painterResource(Res.drawable.material)) {
            materialDialog()
        }
        Item("New textured Material", painterResource(Res.drawable.newmaterial)) {
            textureDialog(state, holder)?.let { file ->
                val material = Material(file.name, file.path)
                holder.addMaterial(material)
            }
        }
    }
}

@Composable
private fun MenuBarScope.models(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val entitiesDialog = EntitiesDialog(holder)
    Menu("Models") {
        Item("Models Library", icon = painterResource(Res.drawable.cube)) {
            entitiesDialog()
        }
        // TODO: icon
        Item("Quick insert model", painterResource(Res.drawable.cubeplus)) {
//            entitiesDialog(state, holder)?.let { entityModel ->
//                holder.instantiateEntity(entityModel)
//            }
        }
    }
}

@Composable
private fun MenuBarScope.selection(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Menu("Selection") {
        Item("Apply current material", icon = painterResource(Res.drawable.applymat)) {
            holder.applyMaterialToSelection()
        }
        val texturingDialog = texturingDialog(holder)
        Item("Texture positioning", icon = painterResource(Res.drawable.texsetup)) {
            texturingDialog()
        }
        Separator()
        val groupable = state.brushSelection.size > 1 &&
                (state.brushSelection.any { model.brushGroups[it] == null } ||
                        state.brushSelection.mapNotNull { model.brushGroups[it] }.toSet().size > 1)
        Item("Group", icon = painterResource(Res.drawable.group), enabled = groupable) {
            holder.groupSelection()
        }
        val anyGroups = state.brushSelection.any { model.brushGroups[it] != null }
        Item("Ungroup", icon = painterResource(Res.drawable.ungroup), enabled = anyGroups) {
            holder.ungroupSelection()
        }
        Separator()
        val hidable = !model.invisibleBrushes.containsAll(state.brushSelection)
        Item("Hide", icon = painterResource(Res.drawable.noeye), enabled = hidable) {
            holder.hideSelection()
        }
        val showable = state.brushSelection.any { model.invisibleBrushes.contains(it) }
        Item("Unhide", icon = painterResource(Res.drawable.eye), enabled = showable) {
            holder.unhideSelection()
        }
        Separator()
        val noCarveDialog = okDialog("Carve", "Intersection objects not found")
        // TODO icon
        Item("Carve", icon = painterResource(Res.drawable.minus), enabled = (state.brushSelection.isNotEmpty())) {
            if (!holder.carve()) {
                noCarveDialog()
            }
        }
    }
}

