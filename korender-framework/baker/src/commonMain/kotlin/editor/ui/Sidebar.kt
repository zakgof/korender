package editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import editor.model.Model
import editor.state.State
import editor.state.StateHolder

@Composable
fun Sidebar(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Column(
        Modifier.background(Color.DarkGray)
            .fillMaxHeight()
            .padding(2.dp)
    ) {
        GroupBox("Mode") {
            RadioButtonRow(
                listOf("N", "S", "D"),
                state.mouseMode.ordinal,
                {
                    holder.setMouseMode(State.MouseMode.entries.toTypedArray()[it])
                }
            )
        }
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
        ObjectListView(model, state, holder)
    }
}

@Composable
fun ObjectListView(model: Model, state: State, holder: StateHolder) {
    LazyColumn {
        items(model.brushes.size) { index ->
            val brush = model.brushes[index]
            Text(
                text = brush.name,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        holder.selectBrush(brush)
                    },
                fontWeight = if (state.selectedBrush === brush) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RadioButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
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

@Composable
fun GroupBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .border(1.dp, Color.Gray)
                .padding(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            content()
        }

        Row(
            modifier = Modifier
                .padding(start = 12.dp)
                .background(MaterialTheme.colors.background)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.subtitle1,
                color = Color.Gray
            )
        }
    }
}
