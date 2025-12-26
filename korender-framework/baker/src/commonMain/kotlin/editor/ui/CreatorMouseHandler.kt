package editor.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import editor.model.Brush
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs

internal class CreatorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val holder: StateHolder
) : MouseHandler {

    companion object {
        var frozenCorner: Offset? = null
        var draggedPoint: Offset? = null
        var originalBrush: Brush? = null
    }

    override fun onClick(current: Offset) {
        val rect = mapper.rect(state.creatorBrush)
        if (rect.contains(current)) {
            holder.create()
        }
    }

    override fun onDragStart(start: Offset) {
        val rect = mapper.rect(state.creatorBrush)
        val gridSnap = state.gridScale * state.projectionScale * 0.3f
        val oppositeX = if (abs(start.x - rect.left) < gridSnap) rect.right else if (abs(start.x - rect.right) < gridSnap) rect.left else null
        val oppositeY = if (abs(start.y - rect.top) < gridSnap) rect.bottom else if (abs(start.y - rect.bottom) < gridSnap) rect.top else null
        if (oppositeX != null && oppositeY != null) {
            frozenCorner = Offset(oppositeX, oppositeY)
            draggedPoint = null
        } else if (rect.contains(start)) {
            frozenCorner = null
            draggedPoint = start
            originalBrush = state.creatorBrush
        } else {
            frozenCorner = mapper.snap(start)
            draggedPoint = null
        }
    }

    override fun onDrag(current: Offset) {
        val rect = frozenCorner?.let {
            Rect(it, mapper.snap(current))
        } ?: draggedPoint?.let {
            val screenShift = current - it
            val originalRect = mapper.rect(originalBrush!!)
            Rect(
                mapper.snap(originalRect.topLeft + screenShift),
                mapper.snap(originalRect.bottomRight + screenShift)
            )
        }
        rect?.let {
            val minimax = mapper.toW(it, state.creatorBrush)
            holder.setCreator(minimax[0], minimax[1])
        }
    }
}
