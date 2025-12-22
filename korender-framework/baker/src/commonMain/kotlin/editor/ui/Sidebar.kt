package editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import editor.state.State
import editor.state.StateHolder

@Composable
fun Sidebar(holder: StateHolder) {
    val state by holder.state.collectAsState()
    Column(Modifier.background(Color.DarkGray).fillMaxHeight()) {
        Text("Mode")
        RadioButtonRow(
            listOf("N", "S", "D"),
            state.mouseMode.ordinal,
            {
                holder.setMouseMode(State.MouseMode.entries.toTypedArray()[it])
            }
        )
        Row {
            Button(onClick = { holder.setGridScale(state.gridScale / 2f) }) {
                Text("-")
            }
            Text("Grid ${state.gridScale}")
            Button(onClick = { holder.setGridScale(state.gridScale * 2f) }) {
                Text("+")
            }
        }
        Row {
            Button(onClick = { holder.setProjectionScale(state.projectionScale / 2f) }) {
                Text("-")
            }
            Text("Scale ${state.projectionScale}")
            Button(onClick = { holder.setProjectionScale(state.projectionScale * 2f) }) {
                Text("+")
            }
        }
    }
}

@Composable
fun RadioButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, text ->
            Button(
                onClick = { onSelected(index) },
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = if (index == selectedIndex) Color.LightGray else Color.Black
                    )
            ) {
                Text(
                    text = text,
                    color = if (index == selectedIndex) Color.Black else Color.White
                )
            }
        }
    }
}