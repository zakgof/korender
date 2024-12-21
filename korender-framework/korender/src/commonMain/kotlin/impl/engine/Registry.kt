package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.resultOrNull
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@OptIn(DelicateCoroutinesApi::class)
internal class Registry<D, R : AutoCloseable>(private val factory: suspend (D) -> R) {

    private val map = mutableMapOf<D, Deferred<R>>()
    private var unusedKeys = mutableSetOf<D>()

    fun begin() {
        unusedKeys = HashSet(map.keys)
    }

    fun end() {
        unusedKeys.forEach {
            val deferred = map.remove(it)!!
            deferred.resultOrNull()?.close() ?: deferred.cancel()
        }
    }

    operator fun get(decl: D): R? {
        unusedKeys.remove(decl)
        val deferred = map.getOrPut(decl) {
            GlobalScope.async {
                factory(decl)
            }
        }
        return deferred.resultOrNull()
    }

    fun has(decl: D): Boolean = map.containsKey(decl)

}