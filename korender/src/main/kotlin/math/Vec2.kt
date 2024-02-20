package com.zakgof.korender.math

import kotlin.math.sqrt

class Vec2(val x: Float, val y: Float) {

    companion object {
        val ZERO = Vec2(0f, 0f)
        val X = Vec2(1f, 0f)
        val Y = Vec2(0f, 1f)
    }

    operator fun unaryMinus(): Vec2 = Vec2(-x, -y)
    operator fun times(s: Vec2) = x * s.x + y * s.y
    operator fun times(a: Float) = Vec2(a * x, a * y)
    fun multpercomp(s: Vec2) = Vec2(x * s.x, y * s.y)
    operator fun plus(s: Vec2) = Vec2(x + s.x, y + s.y)
    operator fun minus(s: Vec2) = Vec2(x - s.x, y - s.y)
    fun lengthSquared() = x * x + y * y
    fun length() = sqrt(lengthSquared())
    fun normalize(): Vec2 = this * (1f / length())

    override fun toString(): String = "($x, $y)"
}

