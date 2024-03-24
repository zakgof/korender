package com.zakgof.korender.impl.engine

import com.zakgof.korender.FrameInfo
import java.util.LinkedList
import java.util.Queue

internal class FrameInfoManager(private val inventory: Inventory) {

    val startNanos: Long = System.nanoTime()

    private var frameNumber = 0L
    private var prevFrameNano: Long = System.nanoTime()
    private val frames: Queue<Long> = LinkedList()
    fun frame(): FrameInfo {
        val now = System.nanoTime()
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
            frames.poll()
        }
        return 1e9f / frames.average().toFloat()
    }

}
