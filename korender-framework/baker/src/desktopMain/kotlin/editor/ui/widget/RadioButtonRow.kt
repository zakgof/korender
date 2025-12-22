package editor.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import editor.ui.Theme
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun RadioButtonRow(
    options: List<Pair<DrawableResource, String>>,
    selectedIndex: Int,
    modifier: Modifier = Modifier.Companion,
    onSelected: (Int) -> Unit,

    ) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, text ->
            IconButton(
                icon = options[index].first,
                tooltip = options[index].second,
                background = if (index == selectedIndex) Theme.light else Theme.dark
            ) { onSelected(index) }
        }
    }
}