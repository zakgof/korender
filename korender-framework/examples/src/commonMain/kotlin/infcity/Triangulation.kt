package com.zakgof.korender.examples.infcity

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

interface Triangulation {
    val points: List<Vec3>
    val normals: List<Vec3>
    val texs: List<Vec2>
    val indexes: List<Int>
}