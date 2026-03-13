package com.zakgof.korender.baker.editor.util

import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

fun <T> List<T>.sameOrNull(): T? =
    firstOrNull()?.takeIf { first -> all { it == first } }

fun Float.sanity(): String {
    val v = this.toDouble()
    val a = abs(v)
    if (a < 1e-3) return "0"

    val p = floor(log10(a)).toInt()
    val scale = 10.0.pow(3 - p)              // ~0.1% precision
    val r = round(v * scale) / scale

    val out = if (abs(r - v) <= a * 0.001) r else v
    return BigDecimal.valueOf(out).stripTrailingZeros().toPlainString()
}