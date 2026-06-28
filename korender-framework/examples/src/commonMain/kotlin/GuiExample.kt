package com.zakgof.korender.examples


import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Checkbox
import com.zakgof.korender.CheckboxState
import com.zakgof.korender.Joystick
import com.zakgof.korender.JoystickState
import com.zakgof.korender.Korender
import com.zakgof.korender.ProgressBar
import com.zakgof.korender.Slider
import com.zakgof.korender.SliderState
import com.zakgof.korender.TextStyle
import com.zakgof.korender.math.ColorRGBA

@Composable
fun GuiExample() = Korender(resourceLoader = { Res.readBytes("files/$it") }) {
    val statusStyle = TextStyle(height = 36f, color = ColorRGBA(0xF06543A0))
    val widgetStyle = TextStyle(height = 48f, color = ColorRGBA(0x66FF55A0))
    val checkboxState = CheckboxState(true)
    val sliderState = SliderState(30f, 0f, 100f)
    val joystickState = JoystickState()
    Frame {
        TestExchange.report(frameInfo)
        val progress = fract(frameInfo.time * 0.1f)
        Sky(starrySky())
        Gui {
            Column {
                Row {
                    Filler()
                    Stack(padding = 8f) {
                        Image(id = "icon", imageResource = "texture/korender32.png", width = 48f, height = 48f)
                    }
                    Text(id = "header", fontResource = "font/orbitron.ttf", height = 64f, text = "Korender GUI Demo", color = ColorRGBA(0x246EB9A0))
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
                    Text(id = "cbs", text = "${checkboxState.state}", style = statusStyle)
                    Filler()
                }
                Filler()
                Row {
                    Filler()
                    Text(id = "pbl", text = "Progress bar", style = widgetStyle)
                    ProgressBar(id = "pbw", width = 320f, value = progress)
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
                    Text(id = "sll", text = "Slider", style = widgetStyle)
                    Slider(id = "slw", width = 320f, state = sliderState)
                    Filler()
                }
                Row {
                    Filler()
                    Text(id = "sls", text = "${sliderState.position.toInt()}", style = statusStyle)
                    Filler()
                }
                Filler()
                Row {
                    Filler()
                    Text(id = "jl", text = "Joystick", style = widgetStyle)
                    Joystick(id = "jw", state = joystickState, width = 256f)
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
