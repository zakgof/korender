package com.zakgof.korender.impl.engine

import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler

internal class TouchBox(
    private val x: Float,
    private val y: Float,
    private val w: Float,
    private val h: Float,
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
