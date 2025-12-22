package editor.model

import com.zakgof.korender.math.Vec3
import kotlin.math.round

fun rotateVec(v: Vec3, axis: Vec3, angle: Float): Vec3 {
    val cos = kotlin.math.cos(angle)
    val sin = kotlin.math.sin(angle)

    return v * cos +
            axis.cross(v) * sin +
            axis * (axis.dot(v)) * (1f - cos)
}

fun Vec3.snap(gridStep: Float) =
    Vec3(round(x / gridStep) * gridStep, round(y / gridStep) * gridStep, round(z / gridStep) * gridStep)