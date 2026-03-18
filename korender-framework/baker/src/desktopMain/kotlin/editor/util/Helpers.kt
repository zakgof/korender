package com.zakgof.korender.baker.editor.util

import androidx.compose.ui.graphics.Color
import com.zakgof.korender.math.ColorRGBA
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

fun <T> List<T>.sameOrNull(): T? =
    firstOrNull()?.takeIf { first -> all { it == first } }



fun Float.floorSig(digits: Int): Float {
    val s = 10f.pow(floor(log10(this)).toInt() - (digits - 1))
    return floor(this / s) * s
}

fun Float.advanceSig(digits: Int, increment: Float): Float {
    val s = 10f.pow(floor(log10(this)).toInt() - (digits - 1))
    return round(this / s + increment) * s
}

fun Float.roundSane(): Float {
    val order = floor(log10(this)).toInt()
    val base = 10f.pow(order)
    val normalized = this / base
    val rounded = when {
        normalized < 1.5f -> 1f
        normalized < 3.5f -> 2f
        normalized < 7.5f -> 5f
        else -> 10f
    }
    return rounded * base
}

fun Float.floor2(): Float {
    val bits = toBits()
    val exp = (bits ushr 23) and 0xff
    return Float.fromBits(exp shl 23)
}

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

fun Color.toKorender() = ColorRGBA(red, green, blue, alpha)

fun ColorRGBA.toCompose() = Color(r, g, b, a)