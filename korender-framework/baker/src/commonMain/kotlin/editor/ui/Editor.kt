package editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.zakgof.korender.KeyEvent
import editor.state.State
import editor.state.StateHolder
import editor.ui.projection.Axes
import editor.ui.projection.Axes.Companion.Front
import editor.ui.projection.Axes.Companion.Left
import editor.ui.projection.Axes.Companion.Top
import editor.ui.projection.ProjectionView

@Composable
fun BrushEditor() {
    val holder = remember { StateHolder() }
    val state by holder.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var deleteAlert by remember { mutableStateOf<String?>(null) }

    fun onKey(keyEvent: KeyEvent) {
        println("KEY EVENT : ${keyEvent.key}")
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.C && keyEvent.ctrlPressed) {
            holder.copy()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN &&keyEvent.composeKey == Key.V && keyEvent.ctrlPressed) {
            holder.paste()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.Enter && state.mouseMode == State.MouseMode.CREATOR) {
            holder.create()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.Delete && state.selectedBrush != null) {
            deleteAlert = "Delete ${state.selectedBrush!!.name}"
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && State.STATE_KEYS.contains(keyEvent.composeKey)) {
            holder.keyDown(keyEvent.composeKey)
        }
        if (keyEvent.type == KeyEvent.Type.UP && State.STATE_KEYS.contains(keyEvent.composeKey)) {
            holder.keyUp(keyEvent.composeKey)
        }
    }

    Column {
        Row(
            modifier = Modifier
                .focusable()
                .focusRequester(focusRequester)
                .onPreviewKeyEvent {
                    onKey(it.toKorender())
                    true
                }) {
            Column(Modifier.weight(1f)) {
                ProjectionBox(Top, holder, "top")
                Divider(Modifier.fillMaxWidth().height(4.dp))
                ProjectionBox(Left, holder, "left")
            }
            Divider(Modifier.fillMaxHeight().width(4.dp))
            Column(Modifier.weight(1f)) {
                ProjectionBox(Front, holder, "front")
                Divider(Modifier.fillMaxWidth().height(4.dp))
                Box(Modifier.weight(1f).fillMaxSize()) {
                    KorenderView(holder, { onKey(it) })
                }
            }
            Divider(Modifier.fillMaxHeight().width(4.dp))
            Box(Modifier.fillMaxHeight()) {
                Sidebar(holder)
            }
        }
        deleteAlert?.let { deleteText ->
            AlertDialog(
                onDismissRequest = { deleteAlert = null },
                title = { Text("Delete") },
                text = { Text(deleteText) },
                confirmButton = {
                    TextButton(onClick = {
                        deleteAlert = null
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteAlert = null }) {
                        Text("No")
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private fun androidx.compose.ui.input.key.KeyEvent.toKorender(): KeyEvent = KeyEvent(
    when (type) {
        KeyEventType.KeyUp -> KeyEvent.Type.UP
        else -> KeyEvent.Type.DOWN
    },
    "",
    this.key,
    this.isShiftPressed,
    this.isCtrlPressed,
    this.isAltPressed,
    this.isMetaPressed
)

@Composable
fun ColumnScope.ProjectionBox(axes: Axes, holder: StateHolder, label: String) {
    Column(Modifier.weight(1f).fillMaxSize().background(Theme.background)) {
        Text(
            text = label,
            color = Theme.medium,
            modifier = Modifier.background(Theme.background)
        )
        Box(Modifier.weight(1f).fillMaxSize()) {
            ProjectionView(axes, holder)
        }
    }
}
