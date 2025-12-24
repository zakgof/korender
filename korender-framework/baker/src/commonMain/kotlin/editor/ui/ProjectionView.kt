package editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.platform.LocalDensity
import editor.state.State
import editor.state.StateHolder
import kotlin.math.ceil
import kotlin.math.floor

internal interface MouseHandler {
    fun onDragStart(start: Offset)
    fun onDragEnd()
    fun onDragCancel()
    fun onDrag(current: Offset)
}

object NoOpMouseHandler : MouseHandler {
    override fun onDragStart(start: Offset) {}
    override fun onDragEnd() {}
    override fun onDragCancel() {}
    override fun onDrag(current: Offset) {}
}

@Composable
fun ProjectionView(axis: Int, holder: StateHolder) {
    val density = LocalDensity.current
    val state by holder.state.collectAsState()
    var mouseHandler: MouseHandler by remember { mutableStateOf(NoOpMouseHandler) }

    Canvas(
        Modifier
            .background(Color.Black)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { start -> mouseHandler.onDragStart(start) },
                    onDragEnd = { mouseHandler.onDragEnd() },
                    onDragCancel = { mouseHandler.onDragCancel() }
                ) { change, dragAmount ->
                    change.consume()
                    mouseHandler.onDrag(change.position)
                }
            }
    ) {
        val mapper = ProjectionMapper(axis, state, size)
        mouseHandler = mouseHandler(mapper, state, holder)
        drawGrid(mapper, state)
        if (state.mouseMode === State.MouseMode.NEW) {
            drawCreator(mapper, state)
        }

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

private fun mouseHandler(mapper: ProjectionMapper, state: State, holder: StateHolder): MouseHandler = when (state.mouseMode) {
    State.MouseMode.NEW -> CreatorMouseHandler(mapper, state, holder)
    else -> NoOpMouseHandler
}