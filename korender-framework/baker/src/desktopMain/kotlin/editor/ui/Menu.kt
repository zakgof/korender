package editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuBarScope
import com.zakgof.korender.baker.editor.ui.dialog.dryRunDialog
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.copy
import com.zakgof.korender.baker.resources.cut
import com.zakgof.korender.baker.resources.eye
import com.zakgof.korender.baker.resources.file
import com.zakgof.korender.baker.resources.load
import com.zakgof.korender.baker.resources.material
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.newmaterial
import com.zakgof.korender.baker.resources.paste
import com.zakgof.korender.baker.resources.play
import com.zakgof.korender.baker.resources.plus
import com.zakgof.korender.baker.resources.save
import com.zakgof.korender.baker.resources.trash
import com.zakgof.korender.baker.resources.zoomin
import com.zakgof.korender.baker.resources.zoomout
import editor.model.Material
import editor.model.TexId
import editor.state.State
import editor.state.StateHolder
import editor.ui.dialog.MaterialsDialog
import editor.ui.dialog.confirmDialog
import editor.ui.dialog.okDialog
import editor.ui.dialog.textureDialog
import org.jetbrains.compose.resources.painterResource
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun FrameWindowScope.Menu(holder: StateHolder) =
    MenuBar {
        file(holder)
        view(holder)
        edit(holder)
        material(holder)
        tools(holder)
    }

@Composable
private fun MenuBarScope.file(holder: StateHolder) {
    val state by holder.state.collectAsState()
    Menu("File") {
        val newProjectConfirmDialog = confirmDialog("New project", "Discard everything and start a new project ?") {
            holder.newProject()
        }
        Item("New", icon = painterResource(Res.drawable.file)) {
            newProjectConfirmDialog()
        }
        var lastDir by remember { mutableStateOf("") }
        val loadProjectConfirmDialog = confirmDialog("Load Project", "Discard everything and load a project ?") {
            val dialog = FileDialog(Frame(), "Load Project", FileDialog.LOAD)
            dialog.directory = lastDir
            dialog.isVisible = true
            val file = dialog.files.firstOrNull()
            file?.let {
                lastDir = file.parent
                holder.loadProject(file.path)
            }
        }
        Item("Open Project", painterResource(Res.drawable.load)) { loadProjectConfirmDialog() }

        Item("Save Project", painterResource(Res.drawable.save)) {
            if (state.savePath == null) {
                val dialog = FileDialog(Frame(), "Save Project", FileDialog.SAVE)
                dialog.directory = lastDir
                dialog.isVisible = true
                val file = dialog.files.firstOrNull()
                file?.let {
                    lastDir = file.parent
                    holder.saveProject(file.path)
                }
            } else {
                holder.saveProject(state.savePath!!)
            }
        }
        Separator()
        val dryRunDialog = dryRunDialog(holder)
        Item("Dry-Run", painterResource(Res.drawable.play)) {
            holder.dryRun()
            dryRunDialog()
        }
    }
}

@Composable
private fun MenuBarScope.view(holder: StateHolder) {
    val state by holder.state.collectAsState()
    Menu("View") {
        Item("Coarser grid", painterResource(Res.drawable.plus), shortcut = KeyShortcut(Key.LeftBracket, ctrl = true)) {
            holder.setGridScale(state.gridScale * 2f)
        }
        Item("Finer grid", painterResource(Res.drawable.minus), shortcut = KeyShortcut(Key.RightBracket, ctrl = true)) {
            holder.setGridScale(state.gridScale / 2f)
        }
        Separator()
        Item("Zoom in", painterResource(Res.drawable.zoomin), shortcut = KeyShortcut(Key.Plus, ctrl = true)) {
            holder.setProjectionScale(state.projectionScale * 2f)
        }
        Item("Zoom out", painterResource(Res.drawable.zoomout), shortcut = KeyShortcut(Key.Minus, ctrl = true)) {
            holder.setProjectionScale(state.projectionScale / 2f)
        }
        Separator()
        Item("Center selection", painterResource(Res.drawable.eye), shortcut = KeyShortcut(Key.E, ctrl = true)) {
            holder.zoomOnSelection()
        }
    }
}

@Composable
private fun MenuBarScope.edit(holder: StateHolder) {
    val state by holder.state.collectAsState()
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
        Item("Paste", painterResource(Res.drawable.paste), enabled = (state.selection.isNotEmpty()), shortcut = KeyShortcut(Key.V, ctrl = true)) {
            holder.paste()
        }
        val deleteDialog = confirmDialog("Delete", "Delete selected objects ?") { holder.deleteSelected() }
        Item("Delete", painterResource(Res.drawable.trash), shortcut = KeyShortcut(Key.Delete) ) {
            deleteDialog()
        }
    }
}

@Composable
private fun MenuBarScope.material(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val materialDialog = MaterialsDialog(holder)
    Menu("Material") {
        Item("Edit Materials", icon = painterResource(Res.drawable.material)) {
            materialDialog()
        }
        Item("New textured Material", painterResource(Res.drawable.newmaterial)) {
            textureDialog(state, holder)?.let { file ->
                val material = Material(file.name, TexId(file.path))
                holder.addMaterial(material)
            }
        }
    }
}

@Composable
private fun MenuBarScope.tools(holder: StateHolder) {
    val state by holder.state.collectAsState()
    Menu("Tools") {
        val noCarveDialog = okDialog("Carve", "Intersection objects not found")
        Item("Carve", icon = painterResource(Res.drawable.minus), enabled = (state.selection.size == 1)) {
            if (!holder.carve()) {
                noCarveDialog()
            }
        }
    }
}

