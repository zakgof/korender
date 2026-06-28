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
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.x
import com.zakgof.korender.math.y
import com.zakgof.korender.onClick
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

private data class City(val name: String, val lat: Float, val lon: Float) {
    val vec: Vec3 = latLonToVec3(lat, lon)
}

private val cities = listOf(
    City("Tokyo", 35.6762f, 139.6503f),
    City("Delhi", 28.7041f, 77.1025f),
    City("Shanghai", 31.2304f, 121.4737f),
    City("San Paulo", -23.5505f, -46.6333f),
    City("Mumbai", 19.0760f, 72.8777f),
    City("Beijing", 39.9042f, 116.4074f),
)

private fun latLonToVec3(latDeg: Float, lonDeg: Float): Vec3 {
    val lat = latDeg * FloatMath.PI / 180f
    val lon = lonDeg * FloatMath.PI / 180f
    return Vec3(cos(lat) * cos(lon), sin(lat), cos(lat) * sin(lon))
}

private fun slerp(q1: Quaternion, q2: Quaternion, t: Float): Quaternion {
    var dot = q1.w * q2.w + q1.r.x * q2.r.x + q1.r.y * q2.r.y + q1.r.z * q2.r.z
    var q2s = q2
    if (dot < 0) {
        dot = -dot
        q2s = -q2s
    }
    val theta = acos(dot.coerceIn(-1f, 1f))
    if (theta < 0.001f) {
        val s = 1f - t
        return Quaternion(s * q1.w + t * q2s.w, q1.r * s + q2s.r * t).normalize()
    }
    val sinTheta = sin(theta)
    val wa = sin((1f - t) * theta) / sinTheta
    val wb = sin(t * theta) / sinTheta
    return Quaternion(wa * q1.w + wb * q2s.w, q1.r * wa + q2s.r * wb)
}

private fun smoothstep(t: Float) = t * t * (3f - 2f * t)

@Composable
fun NightEarth() {
    var currentLook by remember { mutableStateOf(cities[0].vec) }
    var targetVec by remember { mutableStateOf(cities[0].vec) }

    Korender(resourceLoader = { Res.readBytes("files/$it") }) {
        Frame {
            AmbientLight(white(2.4f))
            camera = camera(-3.x, 1.x, 1.y)
            projection = projection(width.toFloat() / height.toFloat(), 1f, 1f, 10f)

            Renderable(
                base {
                    colorTexture = texture("texture/nightearth.jpg")
                },
                mesh = sphere(),
                transform = rotate(1.x, FloatMath.PI)
                   // .rotate(1.y, FloatMath.PIdiv2)
                  //  .rotate(Quaternion.lookAt(currentLook, 1.y))
            )
            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                    Row {
                        Filler()
                        cities.forEachIndexed { index, city ->
                            if (index > 0) Filler()
                            Text(
                                id = "city_$index",
                                text = city.name,
                                style = TextStyle(height = 24, color = ColorRGBA(0xF06543A0)),
                                onTouch = { onClick(it) {

                                }}
                            )
                        }
                        Filler()
                    }
                }
            }
            TestExchange.report(frameInfo)
        }
    }
}
