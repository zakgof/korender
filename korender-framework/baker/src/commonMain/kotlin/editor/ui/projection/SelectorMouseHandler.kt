package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.zakgof.korender.math.Vec3
import editor.model.Brush
import editor.model.Model
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs

internal class SelectorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val model: Model,
    private val holder: StateHolder
) : MouseHandler {

    data class Dragged(
        val brush: Brush,
        val originalBrush: Brush,
        val frozenCorner: Offset?,
        val draggedPoint: Offset?
    )

    companion object {
        var dragged: Dragged? = null
    }

    override fun onClick(current: Offset) {
        val hit = model.brushes
            .filter { brush -> mapper.rect(brush).contains(current) }
            .sortedBy { brush -> brush.center * Vec3.unit(mapper.axes.lookAxis) }
        val index = hit.indexOf(state.selectedBrush)
        if (hit.isNotEmpty()) {
            val newSelection = hit[(index + 1) % hit.size]
            holder.selectBrush(newSelection)
        }
    }

    override fun onDragStart(start: Offset) {
        state.selectedBrush?.let { brush ->
            val rect = mapper.rect(brush)
            val gridSnap = state.gridScale * state.projectionScale * 0.3f
            val oppositeX = if (abs(start.x - rect.left) < gridSnap) rect.right else if (abs(start.x - rect.right) < gridSnap) rect.left else null
            val oppositeY = if (abs(start.y - rect.top) < gridSnap) rect.bottom else if (abs(start.y - rect.bottom) < gridSnap) rect.top else null
            if (oppositeX != null && oppositeY != null) {
                dragged = Dragged(brush, brush, Offset(oppositeX, oppositeY), null)
                return
            }
            if (rect.contains(start)) {
                dragged = Dragged(brush, brush, null, start)
                return
            }
        }
        dragged = null
    }

    override fun onDrag(current: Offset) {
        dragged?.let { d ->
            d.frozenCorner?.let {
                applyRect(Rect(it, mapper.snap(current)))
            }
            d.draggedPoint?.let {
                val screenShift = current - it
                val originalRect = mapper.rect(d.originalBrush)
                applyRect(
                    Rect(
                        mapper.snap(originalRect.topLeft + screenShift),
                        mapper.snap(originalRect.bottomRight + screenShift)
                    )
                )
            }
        }
    }

    private fun applyRect(rect: Rect) {
        val minimax = mapper.toW(rect, dragged!!.originalBrush)
        val newBrush = holder.resizeBrush(dragged!!.brush, minimax[0], minimax[1])
        dragged = dragged!!.copy(brush = newBrush)
    }
}
