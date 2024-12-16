package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.getPlatform

internal class FrameInfoManager(private val inventory: Inventory) {

    val startNanos: Long = getPlatform().nanoTime()

    private var frameNumber = 0L
    private var prevFrameNano: Long = getPlatform().nanoTime()
    private val frames = mutableListOf<Long>() // TODO: this needs optimization, linked list
    fun frame(): FrameInfo {
        val now = getPlatform().nanoTime()
        val frameTime = now - prevFrameNano
        frames.add(frameTime)
        val frameInfo =
            FrameInfo(frameNumber, (now - startNanos) * 1e-9f, frameTime * 1e-9f, calcAverageFps())
        prevFrameNano = now
        frameNumber++
        return frameInfo
    }

    private fun calcAverageFps(): Float {
        while (frames.size > 128) {
            frames.removeAt(0)
        }
        return 1e9f / frames.average().toFloat()
    }

}
