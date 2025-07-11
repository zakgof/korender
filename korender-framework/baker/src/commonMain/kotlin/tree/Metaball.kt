package com.zakgof.korender.baker.tree

import com.zakgof.korender.math.FloatMath
import com.zakgof.korender.math.Vec3
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Metaball(
    private val height: Float,
    private val radius: Float,
    private val sphereCount: Int = 4096,
    private val pointCount: Int = 64,
    private val shape: (Float) -> Float
) {

    val spheres: List<Sphere>
    val points: List<Pt>

    init {
        val rnd = Random(1)
        spheres = (0 until sphereCount).flatMap {
            val phi = rnd.nextFloat() * 2f * FloatMath.PI
            val h = rnd.nextFloat() * height
            val r = rnd.nextFloat() * height
            if (abs(r - shape(h)) > 0.6f * radius)
                listOf()
            else
                listOf(Vec3(r * sin(phi), h, r * cos(phi)))
        }.map {
            Sphere(radius, it)
        }
        points = spheres.flatMap { s ->
            (0 until pointCount).map {
                val n = Vec3.random()
                Pt(s.pos + n * s.r, n)
            }
        }
    }

    class Sphere(val r: Float, val pos: Vec3)
    class Pt(val pos: Vec3, val n: Vec3)
}