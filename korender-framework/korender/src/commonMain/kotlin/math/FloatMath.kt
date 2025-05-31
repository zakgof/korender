package com.zakgof.korender.math

import com.zakgof.korender.math.FloatMath.EPSILON
import kotlin.math.abs

object FloatMath {
    const val PI = kotlin.math.PI.toFloat()
    const val PIdiv2 = PI * 0.5f
    const val EPSILON = 0.0001f
}

infix fun Float.near(other: Float): Boolean = abs(this - other) < EPSILON