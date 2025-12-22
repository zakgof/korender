package editor.model

import com.zakgof.korender.math.Vec3

class Brush(
    val min: Vec3,
    val max: Vec3
) {
    val center: Vec3
        get() = (min + max) * 0.5f
}