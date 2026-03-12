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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ProjectionMapper(val axes: Axes, val state: State, val size: Size) {

    val centerX = state.viewCenter * axes.xAxis
    val centerY = state.viewCenter * axes.yAxis

//    fun xVtoW(viewX: Float) = centerX + (viewX - size.width * 0.5f) / state.projectionScale
//    fun yVtoW(viewY: Float) = centerY - (viewY - size.height * 0.5f) / state.projectionScale
    // fun xWtoV(worldX: Float) = size.width * 0.5f + (worldX - centerX) * state.projectionScale
    // fun yWtoV(worldY: Float) = size.height * 0.5f - (worldY - centerY) * state.projectionScale
//    fun xWtoV(v: Vec3) = xWtoV(axes.xAxis * v)
//    fun yWtoV(v: Vec3) = yWtoV(axes.yAxis * v)

    fun wToVx(v: Vec3) = size.width * 0.5f + (axes.xAxis * v - centerX) * state.projectionScale
    fun wToVy(v: Vec3) = size.height * 0.5f + (axes.yAxis * v - centerY) * state.projectionScale
    fun wToV(v: Vec3) = Offset(wToVx(v), wToVy(v))


    private fun snapH(x: Float): Float = size.width * .5f - centerX * state.projectionScale +
            ((x - (size.width * .5f - centerX * state.projectionScale)) /
                    (state.gridScale * state.projectionScale)).roundToInt() * (state.gridScale * state.projectionScale)

    fun snapV(y: Float): Float = size.height * .5f - centerY * state.projectionScale +
            ((y - (size.height * .5f - centerY * state.projectionScale)) /
                    (state.gridScale * state.projectionScale)).roundToInt() * (state.gridScale * state.projectionScale)

    fun snap(start: Offset): Offset = Offset(snapH(start.x), snapV(start.y))


    fun toW(rect: Rect, bb: BoundingBox): BoundingBox {
        val c1 = vToW(rect.topLeft) + axes.lookAxis * (bb.min * axes.lookAxis)
        val c2 = vToW(rect.bottomRight) + axes.lookAxis * (bb.max * axes.lookAxis)
        return BoundingBox.from(c1, c2)
    }

    fun deltaToW(o: Offset) = (axes.xAxis * o.x + axes.yAxis * o.y) / state.projectionScale

    private fun vToW(o: Offset): Vec3 =
        axes.xAxis * ((o.x - size.width * .5f) / state.projectionScale + centerX) +
                axes.yAxis * ((o.y - size.height * .5f) / state.projectionScale + centerY)

    fun rect(bb: BoundingBox): Rect {
        val xmin = wToVx(bb.min)
        val xmax = wToVx(bb.max)
        val ymin = wToVy(bb.min)
        val ymax = wToVy(bb.max)
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

    fun gridDirs(): List<Vec3> = listOf(axes.xAxis, -axes.xAxis, axes.yAxis, -axes.yAxis)

    fun projAngle(a: Vec3, b: Vec3): Float {
        val n = axes.lookAxis
        val cross = a.cross(b)
        val sin = n.dot(cross)
        val cos = a.dot(b) - (a.dot(n) * b.dot(n))
        return atan2(sin, cos)
    }

    fun gridXs(): List<Float> {
        val step = state.gridScale * state.projectionScale
        val origin = size.width * .5f - centerX * state.projectionScale
        val k0 = ceil((0f - origin) / step).toInt()
        val k1 = floor((size.width - origin) / step).toInt()
        return (k0..k1).map { origin + it * step }
    }

    fun gridYs(): List<Float> {
        val step = state.gridScale * state.projectionScale
        val origin = size.height * .5f - centerY * state.projectionScale
        val k0 = kotlin.math.ceil((0f - origin) / step).toInt()
        val k1 = kotlin.math.floor((size.height - origin) / step).toInt()
        return (k0..k1).map { origin + it * step }
    }

    fun zeroGridX() = size.width * .5f - centerX * state.projectionScale

    fun zeroGridY() = size.height * .5f - centerY * state.projectionScale


}
