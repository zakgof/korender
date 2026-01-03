package editor.ui

import GroupBox
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.drag
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.pen
import com.zakgof.korender.baker.resources.plus
import com.zakgof.korender.baker.resources.pointer
import editor.model.Model
import editor.state.State
import editor.state.StateHolder
import editor.ui.widget.IconButton
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun Sidebar(holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    Column(
        Modifier.background(Theme.background)
            .fillMaxHeight()
            .padding(2.dp)
    ) {
        GroupBox("Mode") {
            RadioButtonRow(
                listOf(Res.drawable.pen, Res.drawable.pointer, Res.drawable.drag),
                state.mouseMode.ordinal,
                {
                    holder.setMouseMode(State.MouseMode.entries.toTypedArray()[it])
                }
            )
        }
        GroupBox("Grid") {
            Row {
                IconButton(Res.drawable.minus) { holder.setGridScale(state.gridScale / 2f) }
                Text(
                    text = "" + state.gridScale,
                    fontSize = 12.sp,
                    color = Theme.light,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(64.dp).align(Alignment.CenterVertically)
                )
                IconButton(Res.drawable.plus) { holder.setGridScale(state.gridScale * 2f) }
            }
        }
        GroupBox("Scale") {
            Row {
                IconButton(Res.drawable.minus) { holder.setProjectionScale(state.projectionScale / 2f) }
                Text(
                    text = "" + state.projectionScale,
                    fontSize = 12.sp,
                    color = Theme.light,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(64.dp).align(Alignment.CenterVertically)
                )
                IconButton(Res.drawable.plus) { holder.setProjectionScale(state.projectionScale * 2f) }
            }
        }
        state.selectedBrush?.let { selection ->
            GroupBox("Selection") {
                Column {
                    Text(
                        text = "${selection.max.x - selection.min.x} x ${selection.max.y - selection.min.y} x ${selection.max.z - selection.min.z}",
                        fontSize = 12.sp,
                        color = Theme.light,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                    )
                }
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
    options: List<DrawableResource>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, text ->
            IconButton(
                icon = options[index],
                background = if (index == selectedIndex) Theme.light else Theme.dark
            ) { onSelected(index) }
        }
    }
}

