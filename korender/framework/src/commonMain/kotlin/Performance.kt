package com.zakgof.korender

import java.util.*
import kotlin.system.measureNanoTime

class Performance {

    private val AVERAGING_FRAMES = 128
    private val queues = LinkedHashMap<String, Queue<Float>>()

    fun measure(name: String, block: () -> Unit) {
        val duration = measureNanoTime(block) * 1e-9f
        val queue = queues.computeIfAbsent(name) { LinkedList() }
        queue.add(duration)
        if (queue.size > AVERAGING_FRAMES) {
            queue.remove()
        }
    }

    fun stats(): Map<String, Float> = queues.mapValues { it.value.average().toFloat() }

    fun report() {
        stats().forEach {
            val ms = toMs(it.value);
            println("${it.key} ${ms} ms")
        }
    }

    private fun toMs(value: Float): String = String.format("%.3f", value*1000f)
}
