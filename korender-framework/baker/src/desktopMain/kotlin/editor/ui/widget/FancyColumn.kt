package com.zakgof.korender.baker.editor.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

@Composable
fun FancyColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val maxChildWidth = measurables.maxOfOrNull { it.maxIntrinsicWidth(constraints.maxHeight) } ?: 0
        val columnWidth = maxChildWidth.coerceIn(constraints.minWidth, constraints.maxWidth)

        val placeables = measurables.map { measurable ->
            measurable.measure(
                constraints.copy(
                    minWidth = columnWidth,
                    maxWidth = columnWidth,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity // allow children to wrap height
                )
            )
        }

        val totalHeight = placeables.sumOf { it.height }
        val layoutHeight = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width = columnWidth, height = layoutHeight) {
            var yPosition = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}