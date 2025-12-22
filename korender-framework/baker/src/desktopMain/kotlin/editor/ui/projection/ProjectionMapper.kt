package editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import editor.model.brush.Brush
import editor.state.State
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ProjectionMapper(val axes: Axes, val state: State, val size: Size) {

    val horzAxis = axes.xAxis
    val vertAxis = axes.yAxis

    val centerX = state.viewCenter * Vec3.unit(horzAxis)
    val centerY = state.viewCenter * Vec3.unit(vertAxis)

    fun xVtoW(viewX: Float) = centerX + (viewX - size.width * 0.5f) / state.projectionScale
    fun yVtoW(viewY: Float) = centerY - (viewY - size.height * 0.5f) / state.projectionScale
    fun xWtoV(worldX: Float) = size.width * 0.5f + (worldX - centerX) * state.projectionScale
    fun yWtoV(worldY: Float) = size.height * 0.5f - (worldY - centerY) * state.projectionScale
    fun xWtoV(v: Vec3) = xWtoV(Vec3.unit(horzAxis) * v)
    fun yWtoV(v: Vec3) = yWtoV(Vec3.unit(vertAxis) * v)
    fun wToV(v: Vec3) = Offset(xWtoV(v), yWtoV(v))

    fun snapH(x: Float): Float = xWtoV(round(xVtoW(x) / state.gridScale) * state.gridScale)
    fun snapV(y: Float): Float = yWtoV(round(yVtoW(y) / state.gridScale) * state.gridScale)
    fun snap(start: Offset): Offset = Offset(snapH(start.x), snapV(start.y))
    fun toW(rect: Rect, min: Vec3, max: Vec3): BoundingBox {
        val h1 = xVtoW(rect.left)
        val h2 = xVtoW(rect.right)
        val v1 = yVtoW(rect.top)
        val v2 = yVtoW(rect.bottom)
        val min = Vec3.unit(horzAxis) * min(h1, h2) + Vec3.unit(vertAxis) * min(v1, v2) + Vec3.unit(axes.lookAxis) * (min * Vec3.unit(axes.lookAxis))
        val max = Vec3.unit(horzAxis) * max(h1, h2) + Vec3.unit(vertAxis) * max(v1, v2) + Vec3.unit(axes.lookAxis) * (max * Vec3.unit(axes.lookAxis))
        return BoundingBox(min, max)
    }

    fun toW(offset: Offset): Vec3 =
        Vec3.unit(horzAxis) * (offset.x / state.projectionScale) - Vec3.unit(vertAxis) * (offset.y / state.projectionScale)


    fun rect(bb: BoundingBox): Rect {
        val xmin = xWtoV(bb.min)
        val xmax = xWtoV(bb.max)
        val ymin = yWtoV(bb.min)
        val ymax = yWtoV(bb.max)
        return Rect(
            Offset(min(xmin, xmax), min(ymin, ymax)),
            Offset(max(xmin, xmax), max(ymin, ymax))
        )
    }

    fun rect(brush: Brush) = rect(brush.bb)

    fun rect(brushes: Collection<Brush>): Rect? =
        brushes.map { rect(it) }.reduceOrNull { a, b -> a merge b }

    infix fun Rect.merge(b: Rect) = Rect(
        topLeft = Offset(min(this.left, b.left), min(this.top, b.top)),
        bottomRight = Offset(max(this.right, b.right), max(this.bottom, b.bottom))
    )

    fun snapClosest(snapPoints: List<Offset>): Offset {
        val dx = snapPoints.map { snapH(it.x) - it.x }.minBy { abs(it) }
        val dy = snapPoints.map { snapV(it.y) - it.y }.minBy { abs(it) }
        return Offset(dx, dy)
    }

    fun gridDirs(): List<Vec3> = listOf(
        Vec3.unit(axes.xAxis),
        -Vec3.unit(axes.xAxis),
        Vec3.unit(axes.yAxis),
        -Vec3.unit(axes.yAxis)
    )

    fun projAngle(a: Vec3, b: Vec3): Float {
        val n = Vec3.unit(axes.lookAxis)
        val cross = a.cross(b)
        val sin = n.dot(cross)
        val cos = a.dot(b) - (a.dot(n) * b.dot(n))
        return atan2(sin, cos)
    }


}
