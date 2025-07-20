package com.zakgof.korender.examples.island

import kotlin.random.Random

fun Int.chance(block: () -> Unit) {
    if (Random.nextInt(100) < this) block()
}

fun generateBuilding(generator: CityGenerator, xoffset: Int, yoffset: Int, xsize: Int, ysize: Int, seed: Int = 0) {

    val r = Random(seed)

    val h = 30 + r.nextInt(60)
    val base = h / 6 + r.nextInt(3)
    val main = 2 * h / 4 + r.nextInt(3)
    val loft = h - r.nextInt(3, 6)

    generator.building(xoffset, yoffset, xsize, ysize, h) {
        when (level) {
            0 -> 40.chance { symcorner(r.nextInt(1, 3), r.nextInt(1, 3)) }
            base -> 30.chance { square(r.nextInt(1, 3), r.nextInt(1, 3)) }
            base -> 30.chance { symcorner(r.nextInt(1, 3), r.nextInt(1, 3)) }
            base + 1 -> 40.chance { symcorner(1, 1) }
            base + 3 -> 20.chance {
                symcorner(
                    r.nextInt(1, 3),
                    r.nextInt(1, 3)
                )
            }
            main -> square(r.nextInt(1, 3), r.nextInt(1, 3))
            main + 1 -> 40.chance { symcorner(1, 1) }
            main + 3 -> 40.chance { symcorner(1, 1) }
            loft -> squareTo(r.nextInt(5, 8), r.nextInt(5, 8))
        }
    }
}