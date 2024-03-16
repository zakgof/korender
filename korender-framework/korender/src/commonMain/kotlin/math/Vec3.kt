package com.zakgof.korender.math

import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.asJavaRandom

class Vec3(val x: Float, val y: Float, val z: Float) {

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
        val X = Vec3(1f, 0f, 0f)
        val Y = Vec3(0f, 1f, 0f)
        val Z = Vec3(0f, 0f, 1f)

        fun random() = Vec3(
            Random.asJavaRandom().nextGaussian().toFloat(),
            Random.asJavaRandom().nextGaussian().toFloat(),
            Random.asJavaRandom().nextGaussian().toFloat()
        ).normalize()

    }

    operator fun unaryMinus(): Vec3 = Vec3(-x, -y, -z)
    operator fun times(s: Vec3) = x * s.x + y * s.y + z * s.z
    operator fun rem(s: Vec3) = Vec3(y * s.z - z * s.y, z * s.x - x * s.z, x * s.y - y * s.x)
    operator fun times(a: Float) = Vec3(a * x, a * y, a * z)
    fun multpercomp(s: Vec3) = Vec3(x * s.x, y * s.y, z * s.z)
    operator fun plus(s: Vec3) = Vec3(x + s.x, y + s.y, z + s.z)
    operator fun minus(s: Vec3) = Vec3(x - s.x, y - s.y, z - s.z)
    fun lengthSquared() = x * x + y * y + z * z
    fun length() = sqrt(lengthSquared())
    fun normalize(): Vec3 = this * (1f / length())

    override fun toString(): String = "($x, $y, $z)"
}

val Int.x: Vec3
    get() = Vec3(this.toFloat(), 0f, 0f)
val Int.y: Vec3
    get() = Vec3(0f, this.toFloat(), 0f)
val Int.z: Vec3
    get() = Vec3(0f, 0f, this.toFloat())

val Float.x: Vec3
    get() = Vec3(this, 0f, 0f)
val Float.y: Vec3
    get() = Vec3(0f, this, 0f)
val Float.z: Vec3
    get() = Vec3(0f, 0f, this)


