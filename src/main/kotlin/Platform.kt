package com.zakgof.korender

interface Platform {

    fun run(width: Int, height: Int, init: () -> Unit = {}, frameCallback: () -> Unit = {})
}