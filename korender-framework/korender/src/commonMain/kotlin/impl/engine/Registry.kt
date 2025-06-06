package com.zakgof.korender.impl.engine

import com.zakgof.korender.AsyncContext
import com.zakgof.korender.KorenderException
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.resultOrNull
import impl.engine.ImmediatelyFreeRetentionPolicy
import impl.engine.KeepForeverRetentionPolicy
import impl.engine.Retentionable
import impl.engine.TimeRetentionPolicy
import impl.engine.UntilGenerationRetentionPolicy
import kotlinx.coroutines.Deferred

internal class Registry<D : Retentionable, R : AutoCloseable>(
    private val asyncContext: AsyncContext,
    private val factory: suspend (D) -> R
) {

    private val map = mutableMapOf<D, Entry>()
    private var unusedKeys = mutableSetOf<D>()

    fun begin() {
        unusedKeys = HashSet(map.keys)
    }

    fun end(time: Float, generation: Int) {
        unusedKeys.forEach {
            if (map[it]!!.attemptDelete(it.retentionPolicy, time, generation)) {
                map.remove(it)
            }
        }
    }

    operator fun get(key: D): R? {
        unusedKeys.remove(key)
        val entry = map.getOrPut(key) {
            Entry(
                asyncContext.call {
                    factory(key)
                }
            )
        }
        entry.freeTime = null
        return entry.deferred.resultOrNull()
    }

    fun pending() =
        map.values.count { !it.deferred.isCompleted }

    inner class Entry(val deferred: Deferred<R>, var freeTime: Float? = null) {

        fun attemptDelete(retentionPolicy: RetentionPolicy, currentTime: Float, currentGeneration: Int): Boolean {

            val del = when(retentionPolicy) {
                is KeepForeverRetentionPolicy -> false
                is ImmediatelyFreeRetentionPolicy -> true
                is UntilGenerationRetentionPolicy -> currentGeneration > retentionPolicy.generation
                is TimeRetentionPolicy -> currentTime > (freeTime ?: currentTime) + retentionPolicy.seconds
                else -> throw KorenderException("Unknown retention policy")
            }
            if (freeTime == null) {
                freeTime = currentTime
            }
            if (del) {
                deferred.resultOrNull()?.close() ?: deferred.cancel()
            }
            return del
        }
    }

}