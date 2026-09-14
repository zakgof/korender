package editor.cache

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.scope.FrameScope
import com.zakgof.korender.scope.KorenderScope
import editor.model.entity.EntityInstance
import editor.model.entity.EntityModel
import editor.ui.projection.Axes
import editor.util.BoundingSphere
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
object KorenderCache {

    private val modelInfoFetcher = ModelInfoFetcher()
    private val modelSnapCache = KorenderCacheHolder<EntityModel, ImageBitmap> { entityModel, first, consumer ->
        if (first) {
            val bs = BoundingSphere.fromPoints(entityModel.points)
            val deferredKorenderImage = captureFrame(256, 256) {
                camera = camera(
                    bs.center + (bs.radius + 1f).z,
                    -1.z,
                    1.y
                )
                projection = projection(
                    bs.radius * 2f,
                    bs.radius * 2f,
                    0.5f,
                    2f + bs.radius * 2f,
                    ortho()
                )
                AmbientLight(white(1f))
                Node(resourceLoader = { entityModel.bytes }) {
                    Model(entityModel.name + ".kr")
                }

            }
            deferredKorenderImage.invokeOnCompletion {
                val imageBitmap = deferredKorenderImage.getCompleted().toCompose()
                consumer.accept(imageBitmap)
            }
        }
    }

    private val instanceSnapCache = KorenderCacheHolder<EntityInstanceEntry, ImageBitmap> { entry, first, consumer ->
        if (first) {
            val deferredKorenderImage = captureFrame(256, 256) {
                val depth = abs(entry.instance.bb.size dot entry.axes.lookAxis)
                camera = camera(
                    entry.instance.bb.center - entry.axes.lookAxis * (depth * 0.5f + 2f),
                    entry.axes.lookAxis,
                    -entry.axes.yAxis
                )
                projection = projection(
                    abs(entry.instance.bb.size dot entry.axes.xAxis),
                    abs(entry.instance.bb.size dot entry.axes.yAxis),
                    1f,
                    2f + depth,
                    ortho()
                )
                AmbientLight(white(1f))
                Node(resourceLoader = { entry.model.bytes }) {
                    Model(entry.model.id, entry.instance.transform)
                }
            }
            deferredKorenderImage.invokeOnCompletion {
                println("Capture instance snap complete: $entry")
                val imageBitmap = deferredKorenderImage.getCompleted().toCompose()
                consumer.accept(imageBitmap)
            }
        }
    }

    fun modelSnap(entityModel: EntityModel) = modelSnapCache[entityModel]

    fun instanceSnap(entityInstance: EntityInstance, model: EntityModel, axes: Axes): StateFlow<ImageBitmap?> {
        instanceSnapCache.remove { it.instance.id == entityInstance.id && it.instance.rotateHash() != entityInstance.rotateHash() }
        return instanceSnapCache[EntityInstanceEntry(entityInstance, model, axes)]
    }

    suspend fun entityModelInfo(file: File, modelId: String): ModelInfo =
        modelInfoFetcher.push(file, modelId).await()

    suspend fun entityModelInfo(krBytes: ByteArray, modelId: String): ModelInfo =
        modelInfoFetcher.push(krBytes, modelId).await()

    fun remove(entityModel: EntityModel) {
        modelSnapCache.remove(entityModel)
    }

    fun remove(entityInstance: EntityInstance) {
        instanceSnapCache.remove { it.instance.id == entityInstance.id }
    }

    context(fs: FrameScope)
    fun frame() {
        modelSnapCache.frame()
        instanceSnapCache.frame()
        modelInfoFetcher.frame()
    }
}

private class EntityInstanceEntry(
    val instance: EntityInstance,
    val model: EntityModel,
    val axes: Axes
) {
    override fun toString() = "${instance.rotateHash()}_${axes.name}"
    override fun hashCode() = toString().hashCode()
    override fun equals(other: Any?) = toString() == other.toString()
}

private class ModelInfoFetcher {

    class ModelInfoFetchJob(
        val file: File?,
        val krBytes: ByteArray?,
        val modelId: String,
        val deferred: CompletableDeferred<ModelInfo> = CompletableDeferred()
    )

    val jobs = ConcurrentLinkedQueue<ModelInfoFetchJob>()

    fun push(file: File, modelId: String): Deferred<ModelInfo> {
        val job = ModelInfoFetchJob(file, null, modelId)
        jobs.add(job)
        return job.deferred
    }

    fun push(krBytes: ByteArray, modelId: String): Deferred<ModelInfo> {
        val job = ModelInfoFetchJob(null, krBytes, modelId)
        jobs.add(job)
        return job.deferred
    }

    context(fs: FrameScope)
    fun frame() = with(fs) {
        jobs.peek()?.let { job ->
            Node(resourceLoader = { if (job.file == null) job.krBytes!! else File(job.file.parentFile, it).readBytes() }) {
                Model(if (job.file == null) job.modelId + ".kr" else job.file.name, onUpdate = {
                    job.deferred.complete(it)
                    jobs.remove(job)
                })
            }
        }
    }
}

private class KorenderCacheHolder<K, V>(val loader: KorenderScope.(K, Boolean, Consumer<V>) -> Unit) {

    class State<V>(
        val first: Boolean,
        val state: MutableStateFlow<V?>
    )

    private val states = ConcurrentHashMap<K & Any, State<V>>()

    operator fun get(key: K): StateFlow<V?> = states.getOrPut(key) { State(true, MutableStateFlow(null)) }.state

    fun remove(key: K) = states.remove(key)
    fun remove(predicate: (K) -> Boolean) = states.keys.removeAll(predicate)

    context(fs: FrameScope)
    fun frame() {
        states.entries
            .filter { it.value.state.value == null }
            .forEach { entry ->
                fs.loader(entry.key, entry.value.first) { result -> entry.value.state.update { result } }
                states[entry.key] = State(false, entry.value.state)
            }
    }

    override fun toString() = "Total ${states.size}, pending ${states.count { it.value.state.value == null }}"
}