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
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
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
import java.awt.event.KeyEvent.VK_A
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_Z

@Composable
fun BrushEditor() {
    val holder = remember { StateHolder() }
    val state by holder.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    fun onKey(keyEvent: KeyEvent) {
        if (keyEvent.type == KeyEvent.Type.UP && keyEvent.key == "ENTER" && state.mouseMode == State.MouseMode.CREATOR) {
            holder.create()
        }
        if (keyEvent.type == KeyEvent.Type.DOWN && State.STATE_KEYS.contains(keyEvent.key)) {
            holder.keyDown(keyEvent.key)
        }
        if (keyEvent.type == KeyEvent.Type.UP && State.STATE_KEYS.contains(keyEvent.key)) {
            holder.keyUp(keyEvent.key)
        }
    }

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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private fun androidx.compose.ui.input.key.KeyEvent.toKorender(): KeyEvent = KeyEvent(
    when (type) {
        KeyEventType.KeyUp -> KeyEvent.Type.UP
        else -> KeyEvent.Type.DOWN
    },
    when (key.keyCode.toInt()) {
        in VK_A..VK_Z -> String(CharArray(1) { key.keyCode.toInt().toChar() })
        VK_ENTER -> "ENTER"
        else -> "UNKNOWN"
    }
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
