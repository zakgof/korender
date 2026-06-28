package com.zakgof.korender.examples

import kotlin.math.abs
import kotlin.math.roundToInt

internal fun Float.fixedDecimals(digits: Int): String {
    val scale = (1..digits).fold(1) { acc, _ -> acc * 10 }
    val rounded = (this * scale).roundToInt()
    val sign = if (rounded < 0) "-" else ""
    val absRounded = abs(rounded)
    val whole = absRounded / scale
    val fraction = (absRounded % scale).toString().padStart(digits, '0')
    return if (digits == 0) "$sign$whole" else "$sign$whole.$fraction"
}
