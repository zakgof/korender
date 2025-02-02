package com.zakgof.korender.examples.infcity

import kotlin.random.Random

fun Int.chance(block: () -> Unit) {
    if (Random.nextInt(100) < this) block()
}