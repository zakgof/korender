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

    fun <R> safeBytes(resource: String, block: (ByteArray) -> R?): R? {
        val deferred = loadingMap.getOrPut(resource) {
            CoroutineScope(Dispatchers.Default).async {
                resourceBytes(appResourceLoader, resource)
            }
        }
        return deferred.resultOrNull()?.let { block(it) }?.also { loadingMap.remove(resource) }
    }

    fun unsafeBytes(resource: String): ByteArray? {
        val deferred = loadingMap.getOrPut(resource) {
            CoroutineScope(Dispatchers.Default).async {
                resourceBytes(appResourceLoader, resource)
            }
        }
        return deferred.resultOrNull()
    }

    fun free(resource: String) {
        loadingMap.remove(resource)
        waitingMap.remove(resource)
    }

    fun <R> wait(resource: String, function: () -> Deferred<R>): R? =
        unsafeWait(resource, function)?.also {
            waitingMap.remove(resource)
        }


    @Suppress("UNCHECKED_CAST")
    fun <R> unsafeWait(resource: String, function: () -> Deferred<R>): R? {
        val deferred = waitingMap.getOrPut(resource) {
            function()
        }
        return deferred.resultOrNull() as R?
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