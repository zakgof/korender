package editor.ui

import androidx.compose.ui.geometry.Size
import com.zakgof.korender.math.Vec3
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

}
