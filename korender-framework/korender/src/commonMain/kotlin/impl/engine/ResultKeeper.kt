package com.zakgof.korender.impl.engine

internal class ResultKeeper(var success: Boolean = true) {
    fun fail() {
        success = false
    }
}