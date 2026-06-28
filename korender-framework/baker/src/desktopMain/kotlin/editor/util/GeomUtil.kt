package editor.util

import com.zakgof.korender.math.Vec3
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

fun rotateVec(v: Vec3, axis: Vec3, angle: Float): Vec3 {
    val cos = cos(angle)
    val sin = sin(angle)

    return v * cos +
            axis.cross(v) * sin +
            axis * (axis.dot(v)) * (1f - cos)
}

fun Vec3.snap(gridStep: Float) =
    Vec3(round(x / gridStep) * gridStep, round(y / gridStep) * gridStep, round(z / gridStep) * gridStep)