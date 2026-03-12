package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.zakgof.korender.math.Quaternion
import editor.model.BoundingBox
import editor.model.Model
import editor.model.brush.Brush
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

    private class SelectorDrag(
        val start: Offset,
        val originalSelection: Set<String>
    )

    private class MoveDrag(
        val originalBrushes: Map<String, Brush>,
        val start: Offset,
    )

    private class ResizeDrag(
        val originalBrushes: Map<String, Brush>,
        val corner: Corner,
        val frozenCorner: Offset,
    )

    private class RotateDrag(
        val originalBrushes: Map<String, Brush>,
        val start: Offset,
    )

    companion object {
        var drag: Any? = null
        var now: Offset? = null
    }

    override fun onClick(current: Offset, isCtrlDown: Boolean) {
        val selectionRect = mapper.rect(model.brushes.values.filter { state.selection.contains(it.id) })
        if (selectionRect != null && selectionRect.contains(current) && !isCtrlDown) {
            holder.rotateSelectionModes()
        } else {
            val brushId = model.brushes.values
                .filter { brush -> mapper.rect(brush).contains(current) }
                .minByOrNull { brush -> brush.bb.center * Vec3.unit(mapper.axes.lookAxis) }?.id
            brushId?.let { holder.selectBrushes(setOf(it), isCtrlDown, true) }
            if (brushId == null && !isCtrlDown) holder.clearSelection()
        }
    }

    private class Corner(
        val corner: (Rect) -> Offset,
        val opposite: (Rect) -> Offset,
        val xSign: Int,
        val ySign: Int
    )

    private val corners = listOf(
        Corner(Rect::topLeft, Rect::bottomRight, 1, 1),
        Corner(Rect::topRight, Rect::bottomLeft, -1, 1),
        Corner(Rect::bottomLeft, Rect::topRight, 1, -1),
        Corner(Rect::bottomRight, Rect::topLeft, -1, -1)
    )

    override fun onDragStart(start: Offset) {
        val selectionRect = mapper.rect(model.brushes.values.filter { state.selection.contains(it.id) })
        drag = selectionRect?.let {
            getDragStruct(it, start)
        } ?: SelectorDrag(start, state.selection)
    }

    private fun getDragStruct(selectionRect: Rect, start: Offset): Any? {
        val handleHalfSize = 8
        when (state.selectionMode) {
            State.SelectionMode.RESIZE -> corners.forEach { corner ->
                if (pick(handleHalfSize, corner.opposite(selectionRect), start))
                    return ResizeDrag(state.selection.associateWith { model.brushes[it]!! }, corner, corner.corner(selectionRect))
            }

            State.SelectionMode.ROTATE -> corners.forEach { corner ->
                if (pick(handleHalfSize, corner.corner(selectionRect), start))
                    return RotateDrag(state.selection.associateWith { model.brushes[it]!! }, corner.corner(selectionRect))
            }
        }
        if (selectionRect.contains(start)) {
            return MoveDrag(state.selection.associateWith { model.brushes[it]!! }, start)
        }
        return null
    }

    private fun pick(handleHalfSize: Int, a: Offset, b: Offset) = abs(a.x - b.x) < handleHalfSize && abs(a.y - b.y) < handleHalfSize

    override fun onDrag(current: Offset, isCtrlDown: Boolean) {
        now = current
        when (val d = drag) {
            is MoveDrag -> {
                val shift = current - d.start
                val rect = mapper.rect(d.originalBrushes.values)!!
                val snapPoints = listOf(rect.topLeft + shift, rect.bottomRight + shift)
                val snapDelta = mapper.snapClosest(snapPoints)
                val offset = mapper.deltaToW(shift + snapDelta)
                state.selection.forEach {
                    holder.brushChanged(d.originalBrushes[it]!!.translate(offset))
                }
            }

            is ResizeDrag -> {
                val oldBB = d.originalBrushes.values
                    .map { it.bb }
                    .reduce(BoundingBox::merge)
                val rect = safeRect(d.frozenCorner, current, d.corner)

                val newBB = mapper.toW(rect, oldBB)
                state.selection.forEach {
                    val origBrush = d.originalBrushes[it]!!
                    holder.brushChanged(origBrush.scale(oldBB, newBB))
                }
            }

            is RotateDrag -> {
                val oldBB = d.originalBrushes.values
                    .map { it.bb }
                    .reduce(BoundingBox::merge)
                val center = oldBB.center
                val screenCenter = mapper.wToV(center)
                val angle = - ((current - screenCenter) angleTo (d.start - screenCenter))
                val origBrushes = state.selection.map { d.originalBrushes[it]!! }
                val lookAxis = mapper.axes.lookAxis

                val rotation = Quaternion.fromAxisAngle(lookAxis, angle)
                val dAngle = origBrushes.flatMap { it.faces }
                    .map { rotation * it.plane.normal }
                    .filter { it.dot(lookAxis) < 0.95f }
                    .filter { it.dot(lookAxis) > -0.95f }
                    .flatMap { n -> mapper.gridDirs().map { n to it } }
                    .map { mapper.projAngle(it.first, it.second) }
                    .minBy { abs(it) }
                val da = if (abs(dAngle) < 0.15) dAngle else 0f

                origBrushes.forEach {
                    holder.brushChanged(it.rotate(center, lookAxis, angle + da))
                }
            }

            is SelectorDrag -> {
                val rect = unirect(d.start, current)
                var selection = d.originalSelection
                model.brushes.values.filter {
                    val brushRect = mapper.rect(it)
                    rect.contains(brushRect.topLeft) && rect.contains(brushRect.bottomRight) && rect.contains(brushRect.topRight) && rect.contains(brushRect.bottomLeft)
                }.forEach {
                    if (isCtrlDown)
                        selection = if (selection.contains(it.id)) selection - it.id else selection + it.id
                    else
                        selection = selection + it.id
                }

                holder.selectBrushes(selection, false, false)
            }
        }
    }

    override fun onDragEnd() {
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
