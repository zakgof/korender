package editor.cache

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korender.scope.FrameScope
import com.zakgof.korender.scope.KorenderScope
import editor.model.entity.EntityModel
import editor.util.BoundingSphere
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@OptIn(ExperimentalCoroutinesApi::class)
object KorenderCache {

    private val modelSnapCache = KorenderCacheHolder<EntityModel, ImageBitmap> { entityModel, first, consumer ->
        if (first) {
            println("Capture model snap started: ${entityModel.filename}")
            val bs = BoundingSphere.fromPoints(entityModel.points)
            val deferredKorenderImage = captureFrame(256, 256) {
                camera = camera(
                    bs.center - (bs.radius + 1f).z,
                    1.z,
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
                Node(resourceLoader = { File(it.split("#")[1]).readBytes() }) {
                    Model("${entityModel.id}#${entityModel.filename}")
                }

            }
            deferredKorenderImage.invokeOnCompletion {
                println("Capture model snap complete: ${entityModel.filename}")
                val imageBitmap = deferredKorenderImage.getCompleted().toCompose()
                consumer.accept(imageBitmap)
            }
        }
    }

    fun modelSnap(entityModel: EntityModel) = modelSnapCache.get(entityModel)
    fun remove(entityModel: EntityModel) { modelSnapCache.remove(entityModel) }

    context(fs: FrameScope)
    fun frame() {
        modelSnapCache.frame()
    }
}

private class KorenderCacheHolder<K, V>(val loader: KorenderScope.(K, Boolean, Consumer<V>) -> Unit) {

    class State<V>(
        val first: Boolean,
        val state: MutableStateFlow<V?>
    )

    private val states = ConcurrentHashMap<K & Any, State<V>>()

    fun get(key: K): StateFlow<V?> = states.getOrPut(key) { State(true, MutableStateFlow(null)) }.state

    fun remove(key: K) = states.remove(key)

    context(fs: FrameScope)
    fun frame() {
        states.entries
            .filter { it.value.state.value == null }
            .forEach { entry ->
                fs.loader(entry.key, entry.value.first) { result -> entry.value.state.update { result } }
                states[entry.key] = State(false, entry.value.state)
            }
    }
}