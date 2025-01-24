package com.zakgof.korender.examples.city

import kotlin.math.sqrt
import kotlin.random.Random

fun cityTriangulation(): CityGenerator {
    val generator = CityGenerator()
    for (x in 0 until 16) {
        for (z in 0 until 16) {
            val cf = 50 / sqrt(((x - 8) * (x - 8) + (z - 8) * (z - 8) + 1).toDouble()).toInt()
            val h = 36 + cf + Random.nextInt(10)

            if (h > 50) {

                val base = h / 6 + Random.nextInt(3)
                val main = 2 * h / 4 + Random.nextInt(3)
                val loft = h - Random.nextInt(3, 6)

                generator.building(x * 24 - 192 + 8, z * 24 - 192 + 8, 16, 16, h) {
                    when (level) {
                        0 -> 40.chance { symcorner(Random.nextInt(1, 3), Random.nextInt(1, 3)) }

                        base -> 30.chance { square(Random.nextInt(1, 3), Random.nextInt(1, 3)) }

                        base -> 30.chance { symcorner(Random.nextInt(1, 3), Random.nextInt(1, 3)) }
                        base + 1 -> 40.chance { symcorner(1, 1) }
                        base + 3 -> 20.chance {
                            symcorner(
                                Random.nextInt(1, 3),
                                Random.nextInt(1, 3)
                            )
                        }

                        main -> square(Random.nextInt(1, 3), Random.nextInt(1, 3))
                        main + 1 -> 40.chance { symcorner(1, 1) }
                        main + 3 -> 40.chance { symcorner(1, 1) }

                        loft -> squareTo(Random.nextInt(5, 8), Random.nextInt(5, 8))
                    }
                }
            }
        }
    }
    return generator
}