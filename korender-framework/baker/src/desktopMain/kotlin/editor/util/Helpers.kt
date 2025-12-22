package com.zakgof.korender.baker.editor.util

fun <T> List<T>.sameOrNull(): T? =
    firstOrNull()?.takeIf { first -> all { it == first } }