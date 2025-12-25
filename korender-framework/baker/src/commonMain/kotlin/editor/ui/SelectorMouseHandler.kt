package editor.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.zakgof.korender.math.Vec3
import editor.model.Brush
import editor.model.Model
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class SelectorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val model: Model,
    private val holder: StateHolder
) : MouseHandler {

    data class Frozen(
        val brush: Brush,
        val frozenCorner: Offset?,
        val draggedPoint: Offset?
    )

    companion object {
        var frozen: Frozen? = null
    }

    override fun onClick(current: Offset) {
        val hit = model.brushes
            .filter { brush -> rect(brush).contains(current) }
            .sortedBy { brush -> brush.center * Vec3.unit(mapper.axis) }
        val index = hit.indexOf(state.selectedBrush)
        if (hit.isNotEmpty()) {
            val newSelection = hit[(index + 1) % hit.size]
            holder.selectBrush(newSelection)
        }
    }

    override fun onDragStart(start: Offset) {
        state.selectedBrush?.let { brush ->
            val rect = rect(brush)
            val gridSnap = state.gridScale * state.projectionScale * 0.3f
            val oppositeX = if (abs(start.x - rect.left) < gridSnap) rect.right else if (abs(start.x - rect.right) < gridSnap) rect.left else null
            val oppositeY = if (abs(start.y - rect.top) < gridSnap) rect.bottom else if (abs(start.y - rect.bottom) < gridSnap) rect.top else null
            if (oppositeX != null && oppositeY != null) {
                frozen = Frozen(brush, Offset(oppositeX, oppositeY), null)
                return
            }
            if (rect.contains(start)) {
                frozen = Frozen(brush, null, start)
                return
            }
        }
        frozen = null
    }

    override fun onDragEnd() {
        frozen = null
    }

    override fun onDragCancel() {
        frozen = null
    }

    override fun onDrag(current: Offset) {
        frozen?.let {
            frozen!!.frozenCorner?.let {
                val h1 = mapper.xVtoW(frozen!!.frozenCorner!!.x)
                val h2 = mapper.xVtoW(mapper.snapH(current.x))
                val v1 = mapper.yVtoW(frozen!!.frozenCorner!!.y)
                val v2 = mapper.yVtoW(mapper.snapV(current.y))
                val min = Vec3.unit(mapper.horzAxis) * min(h1, h2) + Vec3.unit(mapper.vertAxis) * min(v1, v2) + Vec3.unit(mapper.axis) * (state.creatorBrush.min * Vec3.unit(mapper.axis))
                val max = Vec3.unit(mapper.horzAxis) * max(h1, h2) + Vec3.unit(mapper.vertAxis) * max(v1, v2) + Vec3.unit(mapper.axis) * (state.creatorBrush.max * Vec3.unit(mapper.axis))
                val newBrush = holder.resizeBrush(frozen!!.brush, min, max)
                frozen = frozen!!.copy(brush = newBrush)
            }
            frozen!!.draggedPoint?.let {
                val shift = current - frozen!!.draggedPoint!!
                val min = frozen!!.brush.min + Vec3.unit(mapper.horzAxis) * (shift.x / state.projectionScale) -
                        Vec3.unit(mapper.vertAxis) * (shift.y / state.projectionScale)
                val max = frozen!!.brush.max + Vec3.unit(mapper.horzAxis) * (shift.x / state.projectionScale) -
                        Vec3.unit(mapper.vertAxis) * (shift.y / state.projectionScale)
                val newBrush = holder.resizeBrush(frozen!!.brush, min, max)
                frozen = frozen!!.copy(brush = newBrush)
            }
        }
    }

    private fun rect(brush: Brush): Rect {
        val xmin = mapper.xWtoV(brush.min)
        val xmax = mapper.xWtoV(brush.max)
        val ymin = mapper.yWtoV(brush.min)
        val ymax = mapper.yWtoV(brush.max)
        return Rect(
            Offset(min(xmin, xmax), min(ymin, ymax)),
            Offset(max(xmin, xmax), max(ymin, ymax))
        )
    }

}
