package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs

internal class CreatorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val holder: StateHolder
) : MouseHandler {

    companion object {
        var drag: Any? = null
    }

    private class ResizeDrag(
        val frozenCorner: Offset
    )

    private class CreatorDrag(
        val draggedPoint: Offset,
        val originalBB: BoundingBox,
    )

    private class GridDrag(
        val originalOffset: Offset,
        val originalCenter: Vec3
    )

    override fun onClick(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) {
        val rect = mapper.rect(state.creator)
        if (rect.contains(current)) {
            holder.create()
        }
    }

    override fun onDragStart(start: Offset, buttons: PointerButtons) {
        if (buttons.isPrimaryPressed) {
            val rect = mapper.rect(state.creator)
            val gridSnap = state.gridScale * state.projectionScale * 0.3f
            val oppositeX = if (abs(start.x - rect.left) < gridSnap) rect.right else if (abs(start.x - rect.right) < gridSnap) rect.left else null
            val oppositeY = if (abs(start.y - rect.top) < gridSnap) rect.bottom else if (abs(start.y - rect.bottom) < gridSnap) rect.top else null
            if (oppositeX != null && oppositeY != null) {
                drag = ResizeDrag(Offset(oppositeX, oppositeY))
            } else if (rect.contains(start)) {
                drag = CreatorDrag(start, state.creator)
            } else {
                drag = ResizeDrag(mapper.snap(start))
            }
        } else if (buttons.isSecondaryPressed) {
            drag = GridDrag(start, state.viewCenter)
        }
    }

    override fun onDrag(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) {
        val d = drag
        if (buttons.isPrimaryPressed) {
            when (d) {
                is ResizeDrag -> {
                    val rect = Rect(d.frozenCorner, mapper.snap(current))
                    val bb = mapper.toW(rect, state.creator)
                    holder.setCreator(bb.min, bb.max)
                }

                is CreatorDrag -> {
                    val screenShift = current - d.draggedPoint
                    val originalRect = mapper.rect(d.originalBB)
                    val rect = Rect(
                        mapper.snap(originalRect.topLeft + screenShift),
                        mapper.snap(originalRect.bottomRight + screenShift)
                    )
                    val bb = mapper.toW(rect, state.creator)
                    holder.setCreator(bb.min, bb.max)
                }
            }
        } else if (buttons.isSecondaryPressed && d is GridDrag) {
            val shift = current - d.originalOffset
            holder.setViewCenter(
                d.originalCenter -
                        mapper.axes.xAxis * (shift.x / state.projectionScale) -
                        mapper.axes.yAxis * (shift.y / state.projectionScale)
            )
        }
    }
}
