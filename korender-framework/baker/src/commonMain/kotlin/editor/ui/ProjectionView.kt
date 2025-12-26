package editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import editor.model.Model
import editor.state.State
import editor.state.StateHolder
import kotlin.math.ceil
import kotlin.math.floor

internal interface MouseHandler {
    fun onClick(current: Offset)
    fun onDragStart(start: Offset)
    fun onDrag(current: Offset)
}

object NoOpMouseHandler : MouseHandler {
    override fun onClick(current: Offset) {}
    override fun onDragStart(start: Offset) {}
    override fun onDrag(current: Offset) {}
}

@Composable
fun ProjectionView(axis: Int, holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    var mouseHandler: MouseHandler by remember { mutableStateOf(NoOpMouseHandler) }

    Canvas(
        Modifier
            .background(Color.Black)
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var isDrag = false
                    val dragStart = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                        isDrag = true
                        change.consume()
                        mouseHandler.onDragStart(down.position)
                    }
                    if (isDrag && dragStart != null) {
                        drag(dragStart.id) { change ->
                            change.consume()
                            mouseHandler.onDrag(change.position)
                        }
                    } else {
                        mouseHandler.onClick(down.position)
                    }
                }
            }
    ) {
        val mapper = ProjectionMapper(axis, state, size)
        mouseHandler = mouseHandler(mapper, state, model, holder)
        drawGrid(mapper, state)
        if (state.mouseMode === State.MouseMode.CREATOR) {
            drawCreator(mapper, state)
        }
        drawBrushes(mapper, state, model)
    }
}


private fun DrawScope.drawCreator(mapper: ProjectionMapper, state: State) {
    drawRect(
        color = Color.Red,
        topLeft = Offset(mapper.xWtoV(state.creatorBrush.min), mapper.yWtoV(state.creatorBrush.min)),
        size = Size(
            mapper.xWtoV(state.creatorBrush.max) - mapper.xWtoV(state.creatorBrush.min),
            mapper.yWtoV(state.creatorBrush.max) - mapper.yWtoV(state.creatorBrush.min)
        ),
        style = Stroke(
            width = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    )
}

private fun DrawScope.drawBrushes(mapper: ProjectionMapper, state: State, model: Model) {
    model.brushes.forEach { brush ->
        drawRect(
            color = brush.projectionColor,
            topLeft = Offset(mapper.xWtoV(brush.min), mapper.yWtoV(brush.min)),
            size = Size(
                mapper.xWtoV(brush.max) - mapper.xWtoV(brush.min),
                mapper.yWtoV(brush.max) - mapper.yWtoV(brush.min)
            ),
            style = Stroke(
                width = if (brush === state.selectedBrush) 4f else 2f
            )
        )
    }
}

private fun DrawScope.drawGrid(mapper: ProjectionMapper, state: State) {
    var gridX = mapper.xWtoV(ceil(mapper.xVtoW(0f) / state.gridScale) * state.gridScale)
    while (gridX < size.width) {
        drawLine(
            color = Color.DarkGray,
            start = Offset(gridX, 0f),
            end = Offset(gridX, size.height),
            strokeWidth = 1f
        )
        gridX += state.gridScale * state.projectionScale
    }
    var gridY = mapper.yWtoV(floor(mapper.yVtoW(size.height) / state.gridScale) * state.gridScale)
    while (gridY > 0) {
        drawLine(
            color = Color.DarkGray,
            start = Offset(0f, gridY),
            end = Offset(size.width, gridY),
            strokeWidth = 1f
        )
        gridY -= state.gridScale * state.projectionScale
    }
}

private fun mouseHandler(mapper: ProjectionMapper, state: State, model: Model, holder: StateHolder): MouseHandler = when (state.mouseMode) {
    State.MouseMode.CREATOR -> CreatorMouseHandler(mapper, state, holder)
    State.MouseMode.SELECT -> SelectorMouseHandler(mapper, state, model, holder)
    else -> NoOpMouseHandler
}