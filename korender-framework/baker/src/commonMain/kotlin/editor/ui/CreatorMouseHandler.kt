package editor.ui

import androidx.compose.ui.geometry.Offset
import com.zakgof.korender.math.Vec3
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

internal class CreatorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val holder: StateHolder
) : MouseHandler {

    companion object {
        var frozen: Offset? = null
    }

    private fun snapH(x: Float): Float = mapper.xWtoV(round(mapper.xVtoW(x) / state.gridScale) * state.gridScale)

    private fun snapV(y: Float): Float = mapper.yWtoV(round(mapper.yVtoW(y) / state.gridScale) * state.gridScale)

    override fun onDragStart(start: Offset) {
        val xmin = mapper.xWtoV(state.creatorBrush.min)
        val xmax = mapper.xWtoV(state.creatorBrush.max)
        val ymin = mapper.yWtoV(state.creatorBrush.min)
        val ymax = mapper.yWtoV(state.creatorBrush.max)
        val gridSnap = state.gridScale * state.projectionScale * 0.3f
        val oppositeX = if (abs(start.x - xmin) < gridSnap) xmax else if (abs(start.x - xmax) < gridSnap) xmin else null
        val oppositeY = if (abs(start.y - ymin) < gridSnap) ymax else if (abs(start.y - ymax) < gridSnap) ymin else null
        frozen = if (oppositeX != null && oppositeY != null) {
            Offset(oppositeX, oppositeY)
        } else {
            Offset(snapH(start.x), snapV(start.y))
        }
    }

    override fun onDragEnd() {
        frozen = null
    }

    override fun onDragCancel() {
        frozen = null
    }

    override fun onDrag(current: Offset) {
        val h1 = mapper.xVtoW(frozen!!.x)
        val h2 = mapper.xVtoW(snapH(current.x))
        val v1 = mapper.yVtoW(frozen!!.y)
        val v2 = mapper.yVtoW(snapV(current.y))
        val min = Vec3.unit(mapper.horzAxis) * min(h1, h2) + Vec3.unit(mapper.vertAxis) * min(v1, v2) + Vec3.unit(mapper.axis) * (state.creatorBrush.min * Vec3.unit(mapper.axis))
        val max = Vec3.unit(mapper.horzAxis) * max(h1, h2) + Vec3.unit(mapper.vertAxis) * max(v1, v2) + Vec3.unit(mapper.axis) * (state.creatorBrush.max * Vec3.unit(mapper.axis))
        holder.setCreator(min, max)
    }

}
