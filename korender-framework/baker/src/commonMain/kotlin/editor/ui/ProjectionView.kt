package editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import com.zakgof.korender.math.Vec3
import editor.state.State
import editor.state.StateHolder
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun ProjectionView(axis: Int, holder: StateHolder) {
    val density = LocalDensity.current
    val state by holder.state.collectAsState()

    Canvas(Modifier.background(Color.Black).fillMaxSize()) {

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

        var gridX = xWtoV(ceil(xVtoW(0f) / state.gridScale) * state.gridScale)
        while (gridX < size.width) {
            drawLine(
                color = Color.DarkGray,
                start = Offset(gridX, 0f),
                end = Offset(gridX, size.height),
                strokeWidth = 1f
            )
            gridX += state.gridScale * state.projectionScale
        }
        var gridY = yWtoV(floor(yVtoW(size.height) / state.gridScale) * state.gridScale)
        while (gridY > 0) {
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, gridY),
                end = Offset(size.width, gridY),
                strokeWidth = 1f
            )
            gridY -= state.gridScale * state.projectionScale
        }

        if (state.mouseMode === State.MouseMode.NEW) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(xWtoV(state.creatorBrush.min), yWtoV(state.creatorBrush.min)),
                size = Size(
                    xWtoV(state.creatorBrush.max) - xWtoV(state.creatorBrush.min),
                    yWtoV(state.creatorBrush.max) - yWtoV(state.creatorBrush.min)
                ),
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            )
        }
    }
}