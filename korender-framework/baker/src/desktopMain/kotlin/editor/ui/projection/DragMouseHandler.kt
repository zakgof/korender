package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButtons
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

    override fun onDragStart(start: Offset, buttons: PointerButtons) {
        originalOffset = start
        originalCenter = state.viewCenter
    }

    override fun onDrag(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) {
        originalOffset?.let { oo ->
            val shift = current - oo
            holder.setViewCenter(originalCenter!! -
                    mapper.axes.xAxis * (shift.x / state.projectionScale) -
                    mapper.axes.yAxis * (shift.y / state.projectionScale))
        }
    }

}
