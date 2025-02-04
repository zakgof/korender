package com.zakgof.korender.examples.infcity

import kotlin.random.Random

fun generateBuilding(): CityGenerator {

    val generator = CityGenerator()

    val h = 30 + Random.nextInt(20)
    val base = h / 6 + Random.nextInt(3)
    val main = 2 * h / 4 + Random.nextInt(3)
    val loft = h - Random.nextInt(3, 6)

    generator.building(0, 0, 16, 16, h) {
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
    return generator
}