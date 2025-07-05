package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Platform

internal class FrameInfoManager() {

    val startNanos: Long = Platform.nanoTime()

    private var frameNumber = 0L
    private var prevFrameNano: Long = Platform.nanoTime()
    private val frames = ArrayDeque<Long>()
    fun frame(pending: Int): FrameInfo {
        val now = Platform.nanoTime()
        val frameTime = now - prevFrameNano
        frames.add(frameTime)
        val frameInfo =
            FrameInfo(frameNumber, (now - startNanos) * 1e-9f, frameTime * 1e-9f, calcAverageFps(), pending)
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
