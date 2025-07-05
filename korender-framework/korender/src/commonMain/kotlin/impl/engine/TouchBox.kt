package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler

internal class TouchBox(
    private val x: Int,
    private val y: Int,
    private val w: Int,
    private val h: Int,
    val id: Any?,
    private val handler: TouchHandler
) {
    fun touch(touchEvent: TouchEvent, forced: Boolean): Boolean {
        if (forced || touchEvent.x > x && touchEvent.x < x + w && touchEvent.y > y && touchEvent.y < y + h) {
            handler(touchEvent)
            return true
        }
        return false
    }
}