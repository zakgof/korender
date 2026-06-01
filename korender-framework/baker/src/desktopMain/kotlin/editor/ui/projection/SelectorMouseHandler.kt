package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import editor.model.Model
import editor.model.brush.Brush
import editor.model.entity.EntityInstance
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

/**
 * Drag on empty -> selection by rectangle
 * Drag on selection -> move selection
 * Drag on selection resize handler -> resize selection
 * Drag on selection rotation handler -> rotate selection
 * Drag on selection shear handler -> shear selection
 * Click on selection - change mode
 * Click on unselected - select top
 */
internal class SelectorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val model: Model,
    private val holder: StateHolder,
) : MouseHandler {

    private inner class SelectionMap(
        val originalBrushSelection: Map<String, Brush>,
        val originalEntityInstanceSelection: Map<String, EntityInstance>,
    ) {
        fun rect() = mapper.rect(originalBrushSelection.values + originalEntityInstanceSelection.values)
        fun bb() = (originalBrushSelection.values + originalEntityInstanceSelection.values).map { it.bb }.reduce(BoundingBox::merge)
    }

    private class SelectorDrag(
        val selectionMap: SelectionMap,
        val start: Offset,
    )

    private class MoveDrag(
        val selectionMap: SelectionMap,
        val start: Offset,
    )

    private class ResizeDrag(
        val selectionMap: SelectionMap,
        val corner: Corner,
        val frozenCorner: Offset,
    )

    private class RotateDrag(
        val selectionMap: SelectionMap,
        val start: Offset,
    )

    private class GridDrag(
        val originalOffset: Offset,
        val originalCenter: Vec3,
    )

    companion object {
        var drag: Any? = null
        var now: Offset? = null
    }

    override fun onClick(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) {
        if (buttons.isPrimaryPressed) {
            val boundables = selectedBrushes() + selectedEntityInstances()

            val selectionRect = mapper.rect(boundables)
            if (selectionRect != null && selectionRect.contains(current) && !isCtrlDown) {
                holder.rotateSelectionModes()
            } else {
                val topBoundable = (model.brushes.values
                    .filter { brush -> !model.invisibleBrushes.contains(brush.id) } +
                        model.entityInstances.values)
                    .filter { boundable -> mapper.rect(boundable).contains(current) }
                    .minByOrNull { boundable -> boundable.bb.center * mapper.axes.lookAxis }
                when (topBoundable) {
                    is Brush -> holder.selectBrushes(setOf(topBoundable.id), isCtrlDown, true)
                    is EntityInstance -> holder.selectEntityInstance(setOf(topBoundable.id), isCtrlDown)
                    else -> if (!isCtrlDown) {
                        holder.clearSelection()
                    }
                }
            }
        }
    }

    private fun selectedEntityInstances(): List<EntityInstance> = model.entityInstances.values.filter { state.entityInstanceSelection.contains(it.id) }

    private fun selectedBrushes(): List<Brush> = model.brushes.values.filter { state.brushSelection.contains(it.id) }

    private class Corner(
        val corner: (Rect) -> Offset,
        val opposite: (Rect) -> Offset,
        val xSign: Int,
        val ySign: Int,
    )

    private val corners = listOf(
        Corner(Rect::topLeft, Rect::bottomRight, 1, 1),
        Corner(Rect::topRight, Rect::bottomLeft, -1, 1),
        Corner(Rect::bottomLeft, Rect::topRight, 1, -1),
        Corner(Rect::bottomRight, Rect::topLeft, -1, -1)
    )

    override fun onDragStart(start: Offset, buttons: PointerButtons) {
        if (buttons.isPrimaryPressed) {
            val selectionRect = mapper.rect(selectedBrushes() + selectedEntityInstances())
            drag = selectionRect?.let {
                getDragStruct(it, start)
            } ?: SelectorDrag(selectionMap(), start)
        } else if (buttons.isSecondaryPressed) {
            drag = GridDrag(start, state.viewCenter)
        }
    }

    private fun getDragStruct(selectionRect: Rect, start: Offset): Any? {
        val handleHalfSize = 8
        when (state.selectionMode) {
            State.SelectionMode.RESIZE -> corners.forEach { corner ->
                if (pick(handleHalfSize, corner.opposite(selectionRect), start))
                    return ResizeDrag(selectionMap(), corner, corner.corner(selectionRect))
            }

            State.SelectionMode.ROTATE -> corners.forEach { corner ->
                if (pick(handleHalfSize, corner.corner(selectionRect), start))
                    return RotateDrag(selectionMap(), corner.corner(selectionRect))
            }
        }
        if (selectionRect.contains(start)) {
            return MoveDrag(selectionMap(), start)
        }
        return null
    }

    private fun selectionMap(): SelectionMap =
        SelectionMap(
            state.brushSelection.associateWith { model.brushes[it]!! },
            state.entityInstanceSelection.associateWith { model.entityInstances[it]!! }
        )

    private fun pick(handleHalfSize: Int, a: Offset, b: Offset) = abs(a.x - b.x) < handleHalfSize && abs(a.y - b.y) < handleHalfSize

    override fun onDrag(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) : Boolean {
        val d = drag
        if (buttons.isPrimaryPressed) {
            now = current
            when (d) {
                is MoveDrag -> {
                    val shift = current - d.start
                    val rect = d.selectionMap.rect()!!
                    val snapPoints = listOf(rect.topLeft + shift, rect.bottomRight + shift)
                    val snapDelta = mapper.snapClosest(snapPoints)
                    val offset = mapper.deltaToW(shift + snapDelta)
                    state.brushSelection.forEach {
                        holder.brushChanged(d.selectionMap.originalBrushSelection[it]!!.translate(offset), false)
                    }
                    state.entityInstanceSelection.forEach {
                        holder.translateEntityInstance(d.selectionMap.originalEntityInstanceSelection[it]!!, offset, false)
                    }
                }

                is ResizeDrag -> {
                    val oldBB = d.selectionMap.bb()
                    val rect = safeRect(d.frozenCorner, current, d.corner)

                    val newBB = mapper.toW(rect, oldBB)
                    state.brushSelection.forEach {
                        val origBrush = d.selectionMap.originalBrushSelection[it]!!
                        holder.brushChanged(origBrush.scale(oldBB, newBB), false)
                    }
                    state.entityInstanceSelection.forEach {
                        val origEntityInstance = d.selectionMap.originalEntityInstanceSelection[it]!!
                        holder.scaleEntityInstance(origEntityInstance, oldBB, newBB, false)
                    }
                }

                is RotateDrag -> {
                    val oldBB = d.selectionMap.bb()
                    val center = oldBB.center
                    val screenCenter = mapper.wToV(center)
                    val angle = -((current - screenCenter) angleTo (d.start - screenCenter))
                    val origBrushes = state.brushSelection.map { d.selectionMap.originalBrushSelection[it]!! }
                    val lookAxis = mapper.axes.lookAxis

                    val rotation = Quaternion.fromAxisAngle(lookAxis, angle)
                    // TODO: add entity orientation snapping
                    val da = origBrushes.flatMap { it.faces.values }
                        .map { rotation * it.plane.normal }
                        .filter { it.dot(lookAxis) < 0.95f }
                        .filter { it.dot(lookAxis) > -0.95f }
                        .flatMap { n -> mapper.gridDirs().map { n to it } }
                        .map { mapper.projAngle(it.first, it.second) }
                        .minByOrNull { abs(it) }
                        ?.let { if (abs(it) < 0.15) it else 0f } ?: 0f

                    origBrushes.forEach {
                        holder.brushChanged(it.rotate(center, lookAxis, angle + da), false)
                    }
                    d.selectionMap.originalEntityInstanceSelection.values.forEach {
                        holder.rotateEntityInstance(it, center, lookAxis, angle + da, false)
                    }
                }

                is SelectorDrag -> {
                    val rect = unirect(d.start, current)
                    var brushSelection = d.selectionMap.originalBrushSelection.keys
                    model.brushes.values
                        .filter { !model.invisibleBrushes.contains(it.id) }
                        .filter {
                            val brushRect = mapper.rect(it)
                            rect.contains(brushRect.topLeft) && rect.contains(brushRect.bottomRight) && rect.contains(brushRect.topRight) && rect.contains(brushRect.bottomLeft)
                        }.forEach {
                            if (isCtrlDown)
                                brushSelection = if (brushSelection.contains(it.id)) brushSelection - it.id else brushSelection + it.id
                            else
                                brushSelection += it.id
                        }
                    var entityInstanceSelection = d.selectionMap.originalEntityInstanceSelection.keys
                    model.entityInstances.values
                        .filter {
                            val entityInstanceRect = mapper.rect(it)
                            rect.contains(entityInstanceRect.topLeft) && rect.contains(entityInstanceRect.bottomRight) && rect.contains(entityInstanceRect.topRight) && rect.contains(entityInstanceRect.bottomLeft)
                        }.forEach {
                            if (isCtrlDown)
                                entityInstanceSelection = if (entityInstanceSelection.contains(it.id)) entityInstanceSelection - it.id else entityInstanceSelection + it.id
                            else
                                entityInstanceSelection += it.id
                        }

                    holder.clearSelection()
                    holder.selectBrushes(brushSelection, true, false)
                    holder.selectEntityInstance(entityInstanceSelection, true)
                }
            }
        } else if (buttons.isSecondaryPressed) {
            if (d is GridDrag) {
                val shift = current - d.originalOffset
                holder.setViewCenter(
                    d.originalCenter -
                            mapper.axes.xAxis * (shift.x / state.projectionScale) -
                            mapper.axes.yAxis * (shift.y / state.projectionScale)
                )
            }
        }
        return true
    }

    override fun onDragEnd() {
        holder.pushHistory()
        now = null
        drag = null
    }

    private fun unirect(a: Offset, b: Offset) = Rect(
        Offset(min(a.x, b.x), min(a.y, b.y)),
        Offset(max(a.x, b.x), max(a.y, b.y))
    )


    override fun draw(drawScope: DrawScope) {
        if (drag is SelectorDrag && now != null) {
            val rect = unirect((drag as SelectorDrag).start, now!!)
            with(drawScope) {
                drawRect(
                    color = Color.Green,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 6f))
                    )
                )
            }
        }
    }

    infix fun Offset.angleTo(that: Offset): Float {
        val cross = this.x * that.y - this.y * that.x
        val dot = this.x * that.x + this.y * that.y
        return atan2(cross, dot)
    }

    private fun safeRect(frozenCorner: Offset, current: Offset, corner: Corner): Rect {
        val target = mapper.snap(current)
        val step = state.gridScale * state.projectionScale
        var tx = target.x
        var ty = target.y
        if ((target.x - frozenCorner.x) * corner.xSign < 0.5f * step)
            tx = frozenCorner.x + corner.xSign * step
        if ((target.y - frozenCorner.y) * corner.ySign < 0.5f * step)
            ty = frozenCorner.y + corner.ySign * step
        return unirect(frozenCorner, Offset(tx, ty))
    }
}
