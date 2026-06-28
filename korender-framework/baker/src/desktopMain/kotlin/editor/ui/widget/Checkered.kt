package editor.ui.widget

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.checkerboardWithGradient(
    squareSize: Dp = 8.dp,
    gradient: Brush
) = clipToBounds()
    .drawWithCache {

        val sizePx = squareSize.toPx()
        val cols = (size.width / sizePx).toInt() + 1
        val rows = (size.height / sizePx).toInt() + 1

        onDrawBehind {
            // Draw checkerboard
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val color =
                        if ((row + col) % 2 == 0) Color.LightGray
                        else Color.White

                    drawRect(
                        color = color,
                        topLeft = Offset(col * sizePx, row * sizePx),
                        size = Size(sizePx, sizePx)
                    )
                }
            }

            drawRect(
                brush = gradient,
                size = size,
                blendMode = BlendMode.SrcOver
            )
        }
    }