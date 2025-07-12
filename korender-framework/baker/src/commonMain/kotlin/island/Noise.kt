package island

import kotlin.math.floor
import kotlin.random.Random

class Perlin1D(seed: Int = 0) {
    private val permutation = IntArray(512)

    init {
        val p = IntArray(256) { it }
        p.shuffle(Random(seed))
        for (i in 0 until 512) {
            permutation[i] = p[i % 256]
        }
    }

    private fun fade(t: Float) = t * t * t * (t * (t * 6 - 15) + 10)

    private fun lerp(a: Float, b: Float, t: Float) = a + t * (b - a)


    // Gradient function for 1D (returns +1 or -1)
    private fun grad(hash: Int, x: Float): Float {
        val h = hash and 0xF
        val grad = 1.0f + (h and 7)  // Gradient value from 1 to 8
        return if ((h and 8) == 0) grad * x else -grad * x
    }

    // Perlin noise at coordinate x
    fun noise(x: Float): Float {
        val xi = floor(x).toInt() and 255
        val xf = x - floor(x)

        val u = fade(xf)

        val a = permutation[xi]
        val b = permutation[xi + 1]

        val g1 = grad(a, xf)
        val g2 = grad(b, xf - 1)

        return lerp(g1, g2, u)  // Returns noise in range roughly [-1, 1]
    }
}

class Perlin2D(seed: Int = 0) {
    private val permutation = IntArray(512)

    init {
        val p = IntArray(256) { it }
        p.shuffle(Random(seed))
        for (i in 0 until 512) {
            permutation[i] = p[i % 256]
        }
    }

    // Fade function (smoothstep: 6t^5 - 15t^4 + 10t^3)
    private fun fade(t: Float): Float {
        return t * t * t * (t * (t * 6 - 15) + 10)
    }

    // Linear interpolation
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + t * (b - a)
    }

    // Gradient vectors from hash
    private fun grad(hash: Int, x: Float, y: Float): Float {
        return when (hash and 0x3) {
            0 ->  x + y
            1 -> -x + y
            2 ->  x - y
            else -> -x - y
        }
    }

    // Main noise function
    fun noise(x: Float, y: Float): Float {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)

        val u = fade(xf)
        val v = fade(yf)

        val aa = permutation[permutation[xi] + yi]
        val ab = permutation[permutation[xi] + yi + 1]
        val ba = permutation[permutation[xi + 1] + yi]
        val bb = permutation[permutation[xi + 1] + yi + 1]

        val x1 = lerp(
            grad(aa, xf, yf),
            grad(ba, xf - 1, yf),
            u
        )
        val x2 = lerp(
            grad(ab, xf, yf - 1),
            grad(bb, xf - 1, yf - 1),
            u
        )

        return lerp(x1, x2, v)  // Roughly in range [-1, 1]
    }
}

