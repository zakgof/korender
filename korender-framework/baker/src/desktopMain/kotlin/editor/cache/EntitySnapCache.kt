package editor.cache

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.scope.FrameScope
import editor.model.entity.EntityInstance
import editor.model.entity.EntityModel
import editor.ui.projection.Axes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
object EntitySnapCache {

    data class Job(
        val instance: EntityInstance,
        val model: EntityModel,
        val axes: Axes,
        val signature: String,
        val deferred: CompletableDeferred<ImageBitmap> = CompletableDeferred<ImageBitmap>(),
    )

    val jobs = mutableMapOf<String, Job>()
    val toCapture = mutableListOf<Job>()

    fun dispose(entityInstance: EntityInstance) {
        removeCachedImages(entityInstance.id)
    }

    fun image(entityInstance: EntityInstance, model: EntityModel, axes: Axes): Deferred<ImageBitmap> {
        val key = cacheKey(entityInstance.id, axes)
        val signature = entityInstance.rotateHash()
        jobs[key]?.let { cached ->
            if (cached.signature == signature) {
                return cached.deferred
            }
            jobs.remove(key)?.let { toCapture.remove(it) }
        }
        println("Request snap image: $key, total snap images: ${jobs.size}")
        removeCachedImage(entityInstance.id, axes)
        return jobs.computeIfAbsent(key) {
            println("Snap image cache miss: $key")
            val job = Job(entityInstance, model, axes, signature)
            toCapture += job
            job
        }.deferred
    }

    private fun cacheKey(entityInstanceId: String, axes: Axes) = "${entityInstanceId}_${axes.name}"

    private fun removeCachedImages(entityInstanceId: String) {
        jobs.entries.removeAll { (_, job) -> job.instance.id == entityInstanceId }
        toCapture.removeAll { job -> job.instance.id == entityInstanceId }
    }

    private fun removeCachedImage(entityInstanceId: String, axes: Axes) {
        val key = cacheKey(entityInstanceId, axes)
        jobs.remove(key)?.let { toCapture.remove(it) }
    }

    context(fs: FrameScope)
    fun frame() = with(fs) {

        if (toCapture.isNotEmpty())
            println("Snap image job queue has: ${toCapture.size} jobs")
        toCapture.forEach { job ->
            println("Capture snap image: ${job.instance.hashCode()}_${job.axes.name}")
            val deferredKorenderImage = captureFrame(256, 256) {
                val depth = abs(job.instance.bb.size dot job.axes.lookAxis)
                camera = camera(
                    job.instance.bb.center - job.axes.lookAxis * (depth * 0.5f + 2f),
                    job.axes.lookAxis,
                    -job.axes.yAxis
                )
                projection = projection(
                    abs(job.instance.bb.size dot job.axes.xAxis),
                    abs(job.instance.bb.size dot job.axes.yAxis),
                    1f,
                    2f + depth,
                    ortho()
                )
                AmbientLight(white(1f))
                Node(resourceLoader = { File(it).readBytes() }) {
                    Obj(job.model.filename, job.instance.transform)
                }
            }
            deferredKorenderImage.invokeOnCompletion {
                println("Capture snap image DONE: ${job.instance.hashCode()}_${job.axes.name}")
                val imageBitmap = deferredKorenderImage.getCompleted().toCompose()
                job.deferred.complete(imageBitmap)
            }
        }
        toCapture.clear()
    }
}
