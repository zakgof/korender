package editor.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import editor.state.State
import editor.state.StateHolder
import editor.ui.projection.ProjectionView

@Composable
fun BrushEditor() {
    val holder = remember { StateHolder() }
    val state by holder.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    Row (modifier = Modifier.focusable()
        .focusRequester(focusRequester)
        .onPreviewKeyEvent {
            if (it.type == KeyEventType.KeyUp && it.key == Key.Enter && state.mouseMode == State.MouseMode.CREATOR) {
                holder.create()
            }
            true
        }) {
        Column (Modifier.weight(1f)) {
            Box(Modifier.weight(1f).fillMaxSize()) {
                ProjectionView(0, holder)
            }
            Divider(Modifier.fillMaxWidth().height(4.dp))
            Box(Modifier.weight(1f).fillMaxSize()) {
                ProjectionView(1, holder)
            }
        }
        Divider(Modifier.fillMaxHeight().width(4.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.weight(1f).fillMaxSize()) {
                ProjectionView(2, holder)
            }
            Divider(Modifier.fillMaxWidth().height(4.dp))
            Box(Modifier.weight(1f).fillMaxSize()) {
                KorenderView(holder)
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