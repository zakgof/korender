package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.RetentionPolicy
import impl.engine.ImmediatelyFreeRetentionPolicy
import impl.engine.KeepForeverRetentionPolicy
import impl.engine.Retentionable
import impl.engine.TimeRetentionPolicy
import impl.engine.UntilGenerationRetentionPolicy

internal class Registry<D : Retentionable, R : AutoCloseable>(
    private val factory: (D) -> R?
) {

    private val map = mutableMapOf<D, Entry>()

    fun begin() {
        map.values.forEach { it.used = false }
    }

    fun end(time: Float, generation: Int) {
        map.forEach {
            if (!it.value.used && it.value.attemptDelete(it.key.retentionPolicy, time, generation)) {
                map.remove(it.key)
            }
        }
    }

    operator fun get(key: D): R? {
        val existing = map[key]
        if (existing != null) {
            existing.freeTime = null
            existing.used = true
            return existing.value
        }

        val value = factory(key)
        if (value != null) {
            map[key] = Entry(value)
            return value
        }

        return null
    }

    inner class Entry(val value: R, var freeTime: Float? = null, var used: Boolean = true) {

        fun attemptDelete(retentionPolicy: RetentionPolicy, currentTime: Float, currentGeneration: Int): Boolean {

            val del = when (retentionPolicy) {
                is KeepForeverRetentionPolicy -> false
                is ImmediatelyFreeRetentionPolicy -> true
                is UntilGenerationRetentionPolicy -> currentGeneration > retentionPolicy.generation
                is TimeRetentionPolicy -> currentTime > (freeTime ?: currentTime) + retentionPolicy.seconds
                else -> throw KorenderException("Unknown retention policy")
            }
            if (freeTime == null) {
                freeTime = currentTime
            }
            return del
        }
    }

}