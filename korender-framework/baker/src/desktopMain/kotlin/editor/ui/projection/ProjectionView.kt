package editor.ui.projection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.model.Model
import editor.model.brush.Brush
import editor.state.State
import editor.state.StateHolder
import editor.ui.dialog.confirmDialog
import kotlin.math.abs
import kotlin.math.ceil

internal interface MouseHandler {
    fun onClick(current: Offset, isCtrlDown: Boolean) {}
    fun onDragStart(start: Offset) {}
    fun onDrag(current: Offset, isCtrlDown: Boolean) {}
    fun onDragEnd() {}
    fun draw(drawScope: DrawScope) {}
}

object NoOpMouseHandler : MouseHandler

class Axes(val name: String, val xAxis: Vec3, val yAxis: Vec3, val lookAxis: Vec3) {
    companion object {
        val Left = Axes("left", 1.z, -1.y, 1.x)
        val Top = Axes("top", 1.x, 1.z, -1.y)
        val Front = Axes("front", 1.x, -1.y, -1.z)
    }
}

@Composable
fun ProjectionView(axes: Axes, holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    var mouseHandler: MouseHandler by remember { mutableStateOf(NoOpMouseHandler) }
    val focusRequester = remember { FocusRequester() }
    val deleteDialog = confirmDialog("Delete", "Delete selected objects ?") { holder.deleteSelected() }
    Canvas(
        Modifier
            .onSizeChanged { size -> holder.viewResized(axes.name, size.width, size.height) }
            .focusRequester(focusRequester)
            .focusable()
            .clipToBounds()
            .background(Color.Black)
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val event = awaitPointerEvent()
                    focusRequester.requestFocus()
                    var isDrag = false
                    val dragStart = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                        isDrag = true
                        change.consume()
                        mouseHandler.onDragStart(down.position)
                    }
                    if (isDrag && dragStart != null) {
                        drag(dragStart.id) { change ->
                            change.consume()
                            mouseHandler.onDrag(change.position, event.keyboardModifiers.isCtrlPressed)
                        }
                        mouseHandler.onDragEnd()
                    } else {
                        mouseHandler.onClick(down.position, event.keyboardModifiers.isCtrlPressed)
                    }
                }
            }
    ) {
        val mapper = ProjectionMapper(axes, state, size)
        mouseHandler = mouseHandler(mapper, state, model, holder)
        drawGrid(mapper, state)
        if (state.mouseMode === State.MouseMode.CREATOR) {
            drawCreator(mapper, state)
        }
        drawBrushes(mapper, state, model)
        drawSelection(mapper, state, model)
        mouseHandler.draw(this)
    }
}

private fun DrawScope.drawSelection(mapper: ProjectionMapper, state: State, model: Model) {
    mapper.rect(state.selection.map { model.brushes[it]!! })?.let { rect ->
        drawRect(
            color = Color.Green,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 9f))
            )
        )
        when (state.selectionMode) {
            State.SelectionMode.RESIZE -> drawResizeHandler(rect)
            State.SelectionMode.ROTATE -> drawRotateHandler(rect)
        }
    }
}

private fun DrawScope.drawResizeHandler(rect: Rect) {
    val halfSize = 6f
    listOf(rect.topLeft, rect.topRight, rect.bottomLeft, rect.bottomRight).forEach {
        drawRect(
            color = Color.Green,
            topLeft = it - Offset(halfSize, halfSize),
            size = Size(halfSize * 2, halfSize * 2)
        )
    }
}

private fun DrawScope.drawRotateHandler(rect: Rect) {
    val halfSize = 6f
    listOf(rect.topLeft, rect.topRight, rect.bottomLeft, rect.bottomRight).forEach {
        drawOval(
            color = Color.Green,
            topLeft = it - Offset(halfSize, halfSize),
            size = Size(halfSize * 2, halfSize * 2)
        )
    }
}

private fun DrawScope.drawCreator(mapper: ProjectionMapper, state: State) {
    val rect = mapper.rect(state.creator)
    drawRect(
        color = Color.Red,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    )
}


private fun DrawScope.drawGrid(mapper: ProjectionMapper, state: State) {
    val zeroX: Float = mapper.zeroGridX()
    mapper.gridXs().forEach {  gridX ->
        drawLine(
            color = Color.DarkGray,
            start = Offset(gridX, 0f),
            end = Offset(gridX, size.height),
            strokeWidth = if (abs(zeroX - gridX) < state.gridScale * 0.1f) 2f else 1f
        )
    }
    val zeroY: Float = mapper.zeroGridY()
    mapper.gridYs().forEach {  gridY ->
        drawLine(
            color = Color.DarkGray,
            start = Offset(0f, gridY),
            end = Offset(size.width, gridY),
            strokeWidth = if (abs(zeroY - gridY) < state.gridScale * 0.1f) 2f else 1f
        )
    }
}

private fun DrawScope.drawBrushes(mapper: ProjectionMapper, state: State, model: Model) {
    model.brushes.values.forEach { brush -> drawBrush(brush, mapper, state) }
}

private fun DrawScope.drawBrush(brush: Brush, mapper: ProjectionMapper, state: State) {
    brush.mesh.edges.forEach { edge ->
        drawLine(
            color = Color(brush.projectionColor),
            start = mapper.wToV(brush.mesh.points[edge.first]),
            end = mapper.wToV(brush.mesh.points[edge.second]),
            strokeWidth = if (state.selection.contains(brush.id)) 3f else 2f
        )
    }
}

private fun mouseHandler(mapper: ProjectionMapper, state: State, model: Model, holder: StateHolder): MouseHandler = when (state.mouseMode) {
    State.MouseMode.CREATOR -> CreatorMouseHandler(mapper, state, holder)
    State.MouseMode.SELECT -> SelectorMouseHandler(mapper, state, model, holder)
    State.MouseMode.DRAG -> DragMouseHandler(mapper, state, holder)
}