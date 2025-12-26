package editor.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.zakgof.korender.math.Vec3
import editor.model.Brush
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ProjectionMapper(val axis: Int, val state: editor.state.State, val size: Size) {

    val horzAxis = (axis + 1) % 3
    val vertAxis = (axis + 2) % 3

    val centerX = state.viewCenter * Vec3.unit(horzAxis)
    val centerY = state.viewCenter * Vec3.unit(vertAxis)

    fun xVtoW(viewX: Float) = centerX + (viewX - size.width * 0.5f) / state.projectionScale
    fun yVtoW(viewY: Float) = centerY - (viewY - size.height * 0.5f) / state.projectionScale
    fun xWtoV(worldX: Float) = size.width * 0.5f + (worldX - centerX) * state.projectionScale
    fun yWtoV(worldY: Float) = size.height * 0.5f - (worldY - centerY) * state.projectionScale
    fun xWtoV(v: Vec3) = xWtoV(Vec3.unit(horzAxis) * v)
    fun yWtoV(v: Vec3) = yWtoV(Vec3.unit(vertAxis) * v)

    fun snapH(x: Float): Float = xWtoV(round(xVtoW(x) / state.gridScale) * state.gridScale)
    fun snapV(y: Float): Float = yWtoV(round(yVtoW(y) / state.gridScale) * state.gridScale)
    fun snap(start: Offset): Offset = Offset(snapH(start.x), snapV(start.y))
    fun toW(rect: Rect, brush: Brush): Array<Vec3> {
        val h1 = xVtoW(rect.left)
        val h2 = xVtoW(rect.right)
        val v1 = yVtoW(rect.top)
        val v2 = yVtoW(rect.bottom)
        val min = Vec3.unit(horzAxis) * min(h1, h2) + Vec3.unit(vertAxis) * min(v1, v2) + Vec3.unit(axis) * (brush.min * Vec3.unit(axis))
        val max = Vec3.unit(horzAxis) * max(h1, h2) + Vec3.unit(vertAxis) * max(v1, v2) + Vec3.unit(axis) * (brush.max * Vec3.unit(axis))
        return arrayOf(min, max)
    }
    fun rect(brush: Brush): Rect {
        val xmin = xWtoV(brush.min)
        val xmax = xWtoV(brush.max)
        val ymin = yWtoV(brush.min)
        val ymax = yWtoV(brush.max)
        return Rect(
            Offset(min(xmin, xmax), min(ymin, ymax)),
            Offset(max(xmin, xmax), max(ymin, ymax))
        )
    }


}
