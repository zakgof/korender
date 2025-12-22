package editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.zakgof.korender.math.Vec3
import editor.model.StateModel

@Composable
fun ProjectionView(axis: Int, stateModel: StateModel) {
    val density = LocalDensity.current
    Canvas(Modifier.background(Color.Black).fillMaxSize()) {

        val horzAxis = (axis + 1) % 3
        val vertAxis = (axis + 2) % 3

        val centerX = stateModel.state.viewCenter * Vec3.unit(horzAxis)
        val centerY = stateModel.state.viewCenter * Vec3.unit(vertAxis)

        val cols = (size.width * stateModel.state.gridScale / stateModel.state.projectionScale).toInt()
        val rows = (size.height * stateModel.state.gridScale / stateModel.state.projectionScale).toInt()

        repeat(cols) { c ->
            val world = centerX + (c - cols * 0.5f) * stateModel.state.gridScale
            val x = size.width * 0.5f + (world - centerX) * stateModel.state.projectionScale
            drawLine(
                color = Color.DarkGray,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
        }
        repeat(rows) { r ->
            val world = centerY - (r - rows * 0.5f) * stateModel.state.gridScale
            val y = size.height * 0.5f - (world - centerY) * stateModel.state.projectionScale
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}