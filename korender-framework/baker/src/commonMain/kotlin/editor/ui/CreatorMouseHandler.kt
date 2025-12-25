package editor.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.zakgof.korender.math.Vec3
import editor.state.State
import editor.state.StateHolder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class CreatorMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val holder: StateHolder
) : MouseHandler {

    companion object {
        var frozen: Offset? = null
    }

    override fun onClick(current: Offset) {
        val rect = rect()
        if (rect.contains(current)) {
            holder.create()
        }
    }

    override fun onDragStart(start: Offset) {
        val rect = rect()
        val gridSnap = state.gridScale * state.projectionScale * 0.3f
        val oppositeX = if (abs(start.x - rect.left) < gridSnap) rect.right else if (abs(start.x - rect.right) < gridSnap) rect.left else null
        val oppositeY = if (abs(start.y - rect.top) < gridSnap) rect.bottom else if (abs(start.y - rect.bottom) < gridSnap) rect.top else null
        frozen = if (oppositeX != null && oppositeY != null) {
            Offset(oppositeX, oppositeY)
        } else {
            Offset(mapper.snapH(start.x), mapper.snapV(start.y))
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
        val h2 = mapper.xVtoW(mapper.snapH(current.x))
        val v1 = mapper.yVtoW(frozen!!.y)
        val v2 = mapper.yVtoW(mapper.snapV(current.y))
        val min = Vec3.unit(mapper.horzAxis) * min(h1, h2) + Vec3.unit(mapper.vertAxis) * min(v1, v2) + Vec3.unit(mapper.axis) * (state.creatorBrush.min * Vec3.unit(mapper.axis))
        val max = Vec3.unit(mapper.horzAxis) * max(h1, h2) + Vec3.unit(mapper.vertAxis) * max(v1, v2) + Vec3.unit(mapper.axis) * (state.creatorBrush.max * Vec3.unit(mapper.axis))
        holder.setCreator(min, max)
    }

    private fun rect(): Rect {
        val xmin = mapper.xWtoV(state.creatorBrush.min)
        val xmax = mapper.xWtoV(state.creatorBrush.max)
        val ymin = mapper.yWtoV(state.creatorBrush.min)
        val ymax = mapper.yWtoV(state.creatorBrush.max)
        return Rect(
            Offset(min(xmin, xmax), min(ymin, ymax)),
            Offset(max(xmin, xmax), max(ymin, ymax))
        )
    }

}
