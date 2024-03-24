package com.zakgof.korender.impl.engine

internal class Registry<D, R : AutoCloseable>(private val factory: (D) -> R) {

    private val map = mutableMapOf<D, R>()
    private var unusedKeys = mutableSetOf<D>()

    fun begin() {
        unusedKeys = HashSet(map.keys)
    }

    fun end() {
        unusedKeys.forEach {
            map[it]!!.close()
            map.remove(it)
        }
    }

    operator fun get(decl: D): R {
        unusedKeys.remove(decl)
        return map.computeIfAbsent(decl) { factory(it) }
    }

    fun has(decl: D): Boolean = map.containsKey(decl)

}