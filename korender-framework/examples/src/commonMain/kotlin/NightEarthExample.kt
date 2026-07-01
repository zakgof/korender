package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.TextStyle
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.Transform.Companion.scale
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.slerp
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.onClick
import com.zakgof.korender.scope.GuiContainerScope
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

private data class City(val name: String, val lat: Float, val lon: Float) {
    val look: Vec3 =
        latLonToVec3(lat, lon)

    private fun latLonToVec3(latDeg: Float, lonDeg: Float): Vec3 {
        val lat = latDeg * FloatMath.PI / 180f
        val lon = lonDeg * FloatMath.PI / 180f
        return Vec3(-cos(lat) * cos(lon), sin(lat), -cos(lat) * sin(lon))
    }
}

private val NewYork = City("New York", 40.7128f, -74.0060f)
private val SaoPaulo = City("San Paulo", -23.5505f, -46.6333f)
private val London = City("London", 51.5074f, -0.1278f)
private val Cairo = City("Cairo", 30.0444f, 31.2357f)
private val Tokyo = City("Tokyo", 35.6762f, 139.6503f)
private val Sydney = City("Sydney", -33.8688f, 151.2093f)
private val Johannesburg = City("Johannesburg", -26.2041f, 28.0473f)

private val cities = listOf(NewYork, SaoPaulo, London, Cairo, Tokyo, Sydney, Johannesburg)

@Composable
fun NightEarth() {
    var look by remember { mutableStateOf(cities[0].look) }
    var alt by remember { mutableStateOf(0f) }
    var focus by remember { mutableStateOf<City?>(null) }

    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        Frame {
            AmbientLight(white(0.8f))
            val (targetLook, targetAlt) = if (focus == null) {
                Vec3(cos(frameInfo.time), 0f, sin(frameInfo.time)) to 150f
            } else {
                focus!!.look to abs(acos((look dot focus!!.look).coerceIn(-1f, 1f))) * 60f
            }
            look = slerp(look, targetLook, frameInfo.dt).normalize()
            alt += (targetAlt - alt) * frameInfo.dt * 4f
            val up = (1.y - look * look.y).let { if (it.lengthSquared() > 1e-4f) it else 1.x }
            camera = camera(-look * (40f + alt), look, up)
            projection = projection(width.toFloat() / height.toFloat(), 1f, 2f, 300f)

            Renderable(
                base {
                    colorTexture = texture("texture/nightearth.jpg")
                },
                mesh = sphere(),
                transform = scale(30f, -30f, 30f)
            )
            Gui {
                Column {
                    Row {
                        Filler()
                        Column {
                            Filler()
                            (listOf(null) + cities).forEach { city ->
                                cityLink(city) { focus = it }
                            }
                            Filler()
                        }
                    }
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
            TestExchange.report(frameInfo)
        }
    }
}

private fun GuiContainerScope.cityLink(city: City?, selected: (City?) -> Unit) =
    Text(
        id = city?.name ?: "Flyover",
        text = city?.name ?: "> Flyover >",
        style = TextStyle(height = 24, color = ColorRGBA(0xF06543A0)),
        onTouch = {
            onClick(it) { selected(city) }
        }
    )
