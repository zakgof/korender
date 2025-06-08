package com.zakgof.korender.impl.engine

import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.impl.resultOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal class Loader(private val appResourceLoader: ResourceLoader) {

    private val loadingMap = mutableMapOf<String, Deferred<ByteArray>>()
    private val waitingMap = mutableMapOf<String, Deferred<*>>()
    private val syncyMap = mutableMapOf<Any, Deferred<*>>()

    fun load(resource: String): ByteArray? {
        val deferred = loadingMap.getOrPut(resource) {
            CoroutineScope(Dispatchers.Default).async {
                resourceBytes(appResourceLoader, resource)
            }
        }
        return deferred.resultOrNull()?.also {
            loadingMap.remove(resource)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> wait(resource: String, function: () -> Deferred<R>): R? {
        val deferred = waitingMap.getOrPut(resource) {
            function()
        }
        return deferred.resultOrNull()?.also {
            waitingMap.remove(resource)
        } as R?
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> syncy(id: Any, function: suspend (ResourceLoader) -> R): R? {
        val deferred = syncyMap.getOrPut(id) {
            CoroutineScope(Dispatchers.Default).async {
                function(appResourceLoader)
            }
        }
        return deferred.resultOrNull()?.also {
            syncyMap.remove(id)
        } as R?
    }

    fun pending(): Int = loadingMap.size + waitingMap.size + syncyMap.size
}