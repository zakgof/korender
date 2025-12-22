package editor.model

import com.zakgof.korender.math.Vec3

class State {
    var viewCenter: Vec3 = Vec3.ZERO
    var projectionScale: Float = 1f         // pixel per world unit
    var gridScale: Float = 16f               // world units
}