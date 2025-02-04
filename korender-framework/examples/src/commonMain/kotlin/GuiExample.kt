package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Checkbox
import com.zakgof.korender.CheckboxState
import com.zakgof.korender.Joystick
import com.zakgof.korender.JoystickState
import com.zakgof.korender.Korender
import com.zakgof.korender.ProgressBar
import com.zakgof.korender.TextStyle
import com.zakgof.korender.math.ColorRGBA
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GuiExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {
    val statusStyle = TextStyle(height = 36, color = ColorRGBA(0xF06543A0))
    val widgetStyle = TextStyle(height = 48, color = ColorRGBA(0x66FF55A0))
    val checkboxState = CheckboxState(true)
    val joystickState = JoystickState()
    Frame {
        val progress = fract(frameInfo.time * 0.1f)
        Sky(starrySky())
        Gui {
            Column {
                Row {
                    Filler()
                    Image(imageResource = "texture/korender32.png", width = 48, height = 48, marginTop = 8, marginRight = 8)
                    Text(id = "header", fontResource = "font/orbitron.ttf", height = 64, text = "Korender GUI Demo", color = ColorRGBA(0x246EB9A0))
                    Filler()
                }
                Filler()
                Row {
                    Filler()
                    Checkbox(id = "cb", state = checkboxState, text = "Checkbox")
                    Filler()
                }
                Row {
                    Filler()
                    Text(id = "cbs",  text = "${checkboxState.state}", style = statusStyle)
                    Filler()
                }
                Filler()
                Row {
                    Filler()
                    Text(id = "pbl", text = "Progress bar", style = widgetStyle)
                    ProgressBar(id = "pbw", width = 320, value = progress)
                    Filler()
                }
                Row {
                    Filler()
                    Text(id = "pbs", text = "${progress.percent()}", style = statusStyle)
                    Filler()
                }
                Filler()
                Row {
                    Filler()
                    Text(id = "jl", text = "Joystick", style = widgetStyle)
                    Joystick(id = "jw", state = joystickState, width = 256)
                    Filler()
                }
                Row {
                    Filler()
                    Text(id = "js", text = "${joystickState.x.percent()} : ${joystickState.y.percent()}", style = statusStyle)
                    Filler()
                }
                Filler()
            }
        }
    }
}


private fun Float.percent() = (this * 100).toInt()