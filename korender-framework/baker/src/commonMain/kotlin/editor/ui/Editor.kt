package editor.ui

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import editor.state.StateHolder

@Composable
fun BrushEditor() {
    val holder = remember { StateHolder() }
    Row {
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
}