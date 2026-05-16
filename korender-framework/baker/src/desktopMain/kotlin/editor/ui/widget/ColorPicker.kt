package editor.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import editor.util.toCompose
import com.zakgof.korender.math.ColorRGBA
import editor.ui.Theme
import kotlin.math.roundToInt

fun ColorRGBA.toHexArgb(): String {
    fun Float.to255() = (this.coerceIn(0f, 1f) * 255f).roundToInt()
    return "%02X%02X%02X%02X".format(
        a.to255(),
        r.to255(),
        g.to255(),
        b.to255()
    )
}

fun String.toKorenderColorOrNull(): ColorRGBA? {
    if (length != 8) return null
    val v = toLongOrNull(16) ?: return null
    return ColorRGBA(
        ((v shr 16) and 0xFF) / 255f, // R
        ((v shr 8) and 0xFF) / 255f,  // G
        (v and 0xFF) / 255f,          // B
        ((v shr 24) and 0xFF) / 255f  // A
    )
}

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    color: ColorRGBA,
    onColorChanged: (ColorRGBA) -> Unit,
) {

    Column(modifier.padding(6.dp).disabled(disabled)) {

        Row(
            modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FancyClickToTextInput(color.toHexArgb(), validator = { it.toKorenderColorOrNull() != null }) { newText ->
                newText.toKorenderColorOrNull()?.let { onColorChanged(it) }
            }
            Box(Modifier.background(color.toCompose()).size(48.dp, 24.dp))
        }
        if (!disabled) {
            Spacer(Modifier.height(12.dp))

            SliderRow(
                "Red", color.r, 0f..1f,
                { color.copy(r = it) },
                onColorChanged
            )

            SliderRow(
                "Green", color.g, 0f..1f,
                { color.copy(g = it) },
                onColorChanged
            )

            SliderRow(
                "Blue", color.b, 0f..1f,
                { color.copy(b = it) },
                onColorChanged
            )

            SliderRow(
                "Alpha", color.a, 0f..1f,
                { color.copy(a = it) },
                onColorChanged,
                checker = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    colorFunc: (Float) -> ColorRGBA,
    onChange: (ColorRGBA) -> Unit,
    checker: Boolean = false,
) {
    val palette =
        List(100) { i ->
            val value = range.start + i * (range.endInclusive - range.start) * 0.01f
            val rbga = colorFunc(value)
            Color(rbga.r, rbga.g, rbga.b, if (checker) rbga.a else 1f)
        }

    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = Theme.label)
            Text(
                when {
                    range.endInclusive == 360f -> value.toInt().toString()
                    else -> "%.2f".format(value)
                }, style = Theme.label
            )
        }

        Slider(
            value = value,
            onValueChange = { onChange(colorFunc(it)) },
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            colors = SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
                Box(
                    Modifier.padding(top = 4.dp).size(8.dp)
                        .background(Color.White, RoundedCornerShape(2.dp))
                )
            },
            track = {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.Center)
                            .let {
                                val brush = Brush.horizontalGradient(palette)
                                if (checker)
                                    it.checkerboardWithGradient(8.dp, brush)
                                else
                                    it.background(brush)
                            }
                    )
                }
            }
        )
    }

    Spacer(Modifier.height(8.dp))
}

