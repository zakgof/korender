package editor.ui

import YesNoBanner
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.plus
import editor.state.State
import editor.state.StateHolder
import editor.ui.projection.Axes
import editor.ui.projection.Axes.Companion.Front
import editor.ui.projection.Axes.Companion.Left
import editor.ui.projection.Axes.Companion.Top
import editor.ui.projection.ProjectionView
import editor.ui.widget.IconButton

class ConfirmableAction (val text: String, val action: () -> Unit)


@Composable
fun BrushEditor() {
    val holder = remember { StateHolder() }
    val state by holder.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var confirmable by remember { mutableStateOf<ConfirmableAction?>(null) }

    fun onKey(keyEvent: KeyEvent) {
        println("KEY EVENT : ${keyEvent.key}")
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.C && keyEvent.ctrlPressed) {
            holder.copy()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.V && keyEvent.ctrlPressed) {
            holder.paste()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.Enter && state.mouseMode == State.MouseMode.CREATOR) {
            holder.create()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && keyEvent.composeKey == Key.Delete && state.selectedBrush != null) {
            confirmable = ConfirmableAction("Delete ${state.selectedBrush!!.name}") { holder.deleteSelected() }
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
            modifier = Modifier.weight(1f)
                .focusable()
                .focusRequester(focusRequester)
                .onPreviewKeyEvent {
                    onKey(it.toKorender())
                    true
                }
                .disabled(confirmable != null)) {
            Column(Modifier.weight(1f)) {
                ProjectionBox(Top, holder, "top")
                Divider(Modifier.height(4.dp))
                ProjectionBox(Left, holder, "left")
            }
            Divider(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                ProjectionBox(Front, holder, "front")
                Divider(Modifier.height(4.dp))
                Box(Modifier.weight(1f).background(Color.Black)) {
                    if (confirmable == null) {
                        KorenderView(holder, { onKey(it) })
                    }
                }
            }
            Divider(Modifier.width(4.dp))
            Box {
                Sidebar(holder)
            }
        }
        confirmable?.let { cnf ->
            YesNoBanner(cnf.text, {confirmable = null}) {
                confirmable = null
                cnf.action()
            }
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
    Column(Modifier.weight(1f).background(Theme.background)) {
        Text(
            text = label,
            color = Theme.medium,
            modifier = Modifier.background(Theme.background)
        )
        Box(Modifier.weight(1f).background(Color.Black)) {
            ProjectionView(axes, holder)
        }
    }
}

fun Modifier.disabled(disabled: Boolean): Modifier =
    if (!disabled) this
    else
        this
            .alpha(0.1f)
            .focusProperties { canFocus = false }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    }
                }
            }

