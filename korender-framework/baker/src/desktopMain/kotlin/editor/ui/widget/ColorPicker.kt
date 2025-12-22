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
import com.zakgof.korender.math.ColorRGBA
import editor.ui.Theme
import kotlin.math.abs
import kotlin.math.roundToInt

data class HSL(
    val h: Float,   // 0..360
    val s: Float,   // 0..1
    val l: Float,   // 0..1
    val a: Float    // 0..1
) {
    fun toRgba(): ColorRGBA {
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return ColorRGBA(r1 + m, g1 + m, b1 + m, a)
    }
}

fun ColorRGBA.toCompose(): Color =
    Color(r, g, b, a)

fun ColorRGBA.toHSL(): HSL {
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val l = (max + min) / 2f

    if (abs(delta) < 1e-6f)
        return HSL(0f, 0f, l, a)

    val s = delta / (1f - abs(2f * l - 1f))

    var h = when (max) {
        r -> ((g - b) / delta) % 6f
        g -> ((b - r) / delta) + 2f
        else -> ((r - g) / delta) + 4f
    } * 60f

    if (h < 0f) h += 360f

    return HSL(h, s, l, a)
}


fun ColorRGBA.toHexArgb(): String {
    fun Float.to255() = (this.coerceIn(0f, 1f) * 255f).roundToInt()

    return "#%02X%02X%02X%02X".format(
        a.to255(),
        r.to255(),
        g.to255(),
        b.to255()
    )
}


@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    color: ColorRGBA,
    onColorChanged: (ColorRGBA) -> Unit
) {

    Column(modifier.padding(12.dp).disabled(disabled)) {

        val hsl = color.toHSL()

        Row(
            modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(color.toHexArgb(), style = Theme.label)
            Box(Modifier.background(color.toCompose()).size(48.dp, 24.dp))
        }
        if (!disabled) {
            Spacer(Modifier.height(12.dp))

            SliderRow(
                "Hue", hsl.h, 0f..360f,
                { hsl.copy(h = it).toRgba() },
                onColorChanged
            )

            SliderRow(
                "Saturation", hsl.s, 0f..1f,
                { hsl.copy(s = it).toRgba() },
                onColorChanged
            )

            SliderRow(
                "Lightness", hsl.l, 0f..1f,
                { hsl.copy(l = it).toRgba() },
                onColorChanged
            )

            SliderRow(
                "Alpha", hsl.a, 0f..1f,
                { hsl.copy(a = it).toRgba() },
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
    checker: Boolean = false
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
                .height(16.dp),
            colors = SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            track = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .let {
                            val brush = Brush.horizontalGradient(palette)
                            if (checker) it.checkerboardWithGradient(8.dp, brush) else it.background(brush)
                        }
                )
            }
        )
    }

    Spacer(Modifier.height(8.dp))
}

