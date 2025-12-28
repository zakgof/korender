package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import com.zakgof.korender.math.Vec3
import editor.state.State
import editor.state.StateHolder

internal class DragMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val holder: StateHolder
) : MouseHandler {

    companion object {
        var originalOffset: Offset? = null
        var originalCenter: Vec3? = null
    }

    override fun onClick(current: Offset) {
    }

    override fun onDragStart(start: Offset) {
        originalOffset = start
        originalCenter = state.viewCenter
    }

    override fun onDrag(current: Offset) {
        originalOffset?.let { oo ->
            val shift = current - oo
            holder.setViewCenter(originalCenter!! -
                    Vec3.unit(mapper.horzAxis) * (shift.x * state.projectionScale) +
                    Vec3.unit(mapper.vertAxis) * (shift.y * state.projectionScale))
        }
    }

}
