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
        val deferred: CompletableDeferred<ImageBitmap> = CompletableDeferred<ImageBitmap>(),
    )

    val jobs = mutableMapOf<String, Job>()
    val toCapture = mutableListOf<Job>()

    fun image(entityInstance: EntityInstance, model: EntityModel, axes: Axes): Deferred<ImageBitmap> {
        val key = "${entityInstance.hashCode()}_${axes.name}"
        println("Request snap image: $key")
        return jobs.computeIfAbsent(key) {
            println("Snap image cache miss: $key")
            val job = Job(entityInstance, model, axes)
            toCapture += job
            job
        }.deferred
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