package editor.ui.projection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.zakgof.korender.baker.editor.util.advanceSig
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.cache.EntitySnapCache
import editor.model.Model
import editor.model.brush.Brush
import editor.model.entity.EntityInstance
import editor.state.State
import editor.state.StateHolder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.abs

internal interface MouseHandler {
    fun onClick(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) {}
    fun onDragStart(start: Offset, buttons: PointerButtons) {}
    fun onDrag(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean): Boolean = false
    fun onDragEnd() {}
    fun draw(drawScope: DrawScope) {}
}

object NoOpMouseHandler : MouseHandler

class Axes(val name: String, val xAxis: Vec3, val yAxis: Vec3, val lookAxis: Vec3) {
    companion object {
        val Left = Axes("left", 1.z, -1.y, 1.x)
        val Top = Axes("top", 1.x, 1.z, -1.y)
        val Front = Axes("front", 1.x, -1.y, -1.z)
        val AllAxes = listOf(Left, Top, Front)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProjectionView(axes: Axes, holder: StateHolder) {
    val state by holder.state.collectAsState()
    val model by holder.model.collectAsState()
    var mouseHandler: MouseHandler = NoOpMouseHandler
    val focusRequester = remember { FocusRequester() }
    var redrawTick by remember { mutableIntStateOf(0) }

    fun requestRedraw() {
        redrawTick++
    }

    Canvas(
        Modifier
            .onSizeChanged { size -> holder.viewResized(axes.name, size.width, size.height) }
            .focusRequester(focusRequester)
            .focusable()
            .clipToBounds()
            .background(Color.Black)
            .fillMaxSize()
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val delta = -event.changes.first().scrollDelta.y
                val zoom = state.projectionScale.advanceSig(2, delta)
                holder.setProjectionScale(zoom)
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val down = event.changes.first()
                    if (event.type == PointerEventType.Press) {
                        focusRequester.requestFocus()
                        var isDrag = false
                        val dragStart = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                            isDrag = true
                            change.consume()
                            mouseHandler.onDragStart(down.position, event.buttons)
                            requestRedraw()
                        }
                        if (isDrag && dragStart != null) {
                            drag(dragStart.id) { change ->
                                change.consume()
                                if (mouseHandler.onDrag(
                                    change.position,
                                    currentEvent.buttons,
                                    currentEvent.keyboardModifiers.isCtrlPressed
                                )) {
                                    requestRedraw()
                                }
                            }
                            mouseHandler.onDragEnd()
                            requestRedraw()
                        } else {
                            mouseHandler.onClick(
                                down.position,
                                event.buttons,
                                event.keyboardModifiers.isCtrlPressed
                            )
                            requestRedraw()
                        }
                    }
                }
            }
    ) {
        redrawTick
        val mapper = ProjectionMapper(axes, state, size)
        mouseHandler = mouseHandler(mapper, state, model, holder)
        drawGrid(mapper, state)
        if (state.mouseMode === State.MouseMode.CREATOR) {
            drawCreator(mapper, state)
        }
        drawBrushes(mapper, state, model)
        drawGroups(mapper, state, model)
        drawSelection(mapper, state, model)
        drawEntityInstances(mapper, state, model, ::requestRedraw)
        mouseHandler.draw(this)
    }
}

private fun DrawScope.drawSelection(mapper: ProjectionMapper, state: State, model: Model) {
    val boundables = state.brushSelection.map { model.brushes[it]!! } + state.entityInstanceSelection.map { model.entityInstances[it]!! }
    mapper.rect(boundables)?.let { rect ->
        drawRect(
            color = Color.Red,
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
    mapper.gridXs().forEach { gridX ->
        drawLine(
            color = Color.DarkGray,
            start = Offset(gridX, 0f),
            end = Offset(gridX, size.height),
            strokeWidth = if (abs(zeroX - gridX) < state.gridScale * 0.1f) 2f else 1f
        )
    }
    val zeroY: Float = mapper.zeroGridY()
    mapper.gridYs().forEach { gridY ->
        drawLine(
            color = Color.DarkGray,
            start = Offset(0f, gridY),
            end = Offset(size.width, gridY),
            strokeWidth = if (abs(zeroY - gridY) < state.gridScale * 0.1f) 2f else 1f
        )
    }
}

private fun DrawScope.drawBrushes(mapper: ProjectionMapper, state: State, model: Model) {
    model.brushes.values.forEach { brush -> drawBrush(brush, mapper, state, model) }
}

private fun DrawScope.drawGroups(mapper: ProjectionMapper, state: State, model: Model) {
    model.groups.values.forEach { group ->
        val hidden = model.invisibleBrushes.containsAll(group.brushIds)
        val brushes = group.brushIds.map { model.brushes[it]!! }
        val rect = mapper.rect(brushes)!!
        drawRect(
            color = if (hidden) Color.DarkGray else Color.Yellow,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
            )
        )
    }
}

private fun DrawScope.drawBrush(brush: Brush, mapper: ProjectionMapper, state: State, model: Model) {
    val hidden = model.invisibleBrushes.contains(brush.id)
    val selected = state.brushSelection.contains(brush.id)
    brush.mesh.edges.forEach { edge ->
        drawLine(
            color = when {
                hidden -> Color.DarkGray
                selected -> Color.Red
                else -> Color(brush.projectionColor)
            },
            start = mapper.wToV(brush.mesh.points[edge.first]),
            end = mapper.wToV(brush.mesh.points[edge.second]),
            strokeWidth = if (selected) 3f else 2f
        )
    }
}

private fun DrawScope.drawEntityInstances(mapper: ProjectionMapper, state: State, model: Model, requestRedraw: () -> Unit) {
    model.entityInstances.values.forEach { entityInstance -> drawEntityInstance(entityInstance, mapper, state, model, requestRedraw) }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun DrawScope.drawEntityInstance(entityInstance: EntityInstance, mapper: ProjectionMapper, state: State, model: Model, requestRedraw: () -> Unit) {
    // val hidden = model.invisibleBrushes.contains(brush.id)
    val selected = state.entityInstanceSelection.contains(entityInstance.id)
    val bb = entityInstance.bb
    val rect = mapper.rect(bb)
    val deferredImage: Deferred<ImageBitmap> = EntitySnapCache.image(entityInstance, model.entityModels[entityInstance.modelId]!!, mapper.axes)

    if (deferredImage.isCompleted) {
        drawImage(
            image = deferredImage.getCompleted(),
            dstOffset = IntOffset(
                rect.left.toInt(),
                rect.top.toInt()
            ),
            dstSize = IntSize(
                rect.width.toInt(),
                rect.height.toInt()
            )
        )
    } else {
        deferredImage.invokeOnCompletion {
            println("Image ready - redraw requested")
            requestRedraw()
        }
    }
    drawRect(
        color = Color.Red,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 9f))
        )
    )
}

private fun mouseHandler(mapper: ProjectionMapper, state: State, model: Model, holder: StateHolder): MouseHandler = when (state.mouseMode) {
    State.MouseMode.CREATOR -> CreatorMouseHandler(mapper, state, holder)
    State.MouseMode.SELECT -> SelectorMouseHandler(mapper, state, model, holder)
    State.MouseMode.DRAG -> DragMouseHandler(mapper, state, holder)
}
