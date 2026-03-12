package editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import editor.state.StateHolder
import editor.ui.projection.Axes
import editor.ui.projection.Axes.Companion.Front
import editor.ui.projection.Axes.Companion.Left
import editor.ui.projection.Axes.Companion.Top
import editor.ui.projection.ProjectionView

@Composable
fun FrameWindowScope.BrushEditor(holder: StateHolder) {
    Menu(holder)
    Column {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            Column(Modifier.weight(1f)) {
                ProjectionBox(Top, holder, "top")
                Divider(Modifier.height(4.dp))
                ProjectionBox(Left, holder, "left")
            }
            Divider(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                ProjectionBox(Front, holder, "front")
                Divider(Modifier.height(4.dp))

                KorenderBox(holder)
            }
            Divider(Modifier.width(4.dp))
            Box {
                Sidebar(holder)
            }
        }
    }
}

@Composable
private fun ColumnScope.KorenderBox(holder: StateHolder) {
    Box(
        Modifier
            .background(Theme.background)
            .onSizeChanged { size -> holder.viewResized("korender", size.width, size.height) }
            .weight(1f)
    ) {
        KorenderView(holder)
    }
}

@Composable
fun ColumnScope.ProjectionBox(axes: Axes, holder: StateHolder, label: String) {
    var isFocused by remember { mutableStateOf(false) }
    Column(
        Modifier
            .border(2.dp, if (isFocused) Theme.medium else Theme.background)
            .padding(2.dp)
            .background(Theme.background)
            .weight(1f)

    ) {
        Text(
            text = label,
            color = Theme.medium,
            modifier = Modifier.background(Theme.background)
        )
        Box(
            Modifier.weight(1f)
                .onFocusChanged { focusState ->
                    isFocused = focusState.hasFocus
                }
        ) {
            ProjectionView(axes, holder)
        }
    }
}

