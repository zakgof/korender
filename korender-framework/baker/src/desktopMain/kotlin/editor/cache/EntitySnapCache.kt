package editor.cache

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.baker.editor.ui.dialog.collectModelPoints
import com.zakgof.korender.math.ColorRGB.Companion.white
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.scope.FrameScope
import editor.model.entity.EntityInstance
import editor.model.entity.EntityModel
import editor.ui.projection.Axes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
object EntitySnapCache {

    private data class InstanceSnapJob(
        val instance: EntityInstance,
        val model: EntityModel,
        val axes: Axes,
        val signature: String,
        val deferred: CompletableDeferred<ImageBitmap> = CompletableDeferred(),
    )

    @OptIn(ExperimentalUuidApi::class)
    private data class PointsJob(
        val filename: String,
        val uuid: Uuid = Uuid.generateV7(),
        val deferred: CompletableDeferred<List<Vec3>> = CompletableDeferred(),
    ) {
        val uniqueName: String = "$uuid#$filename"
    }

    private val jobs = mutableMapOf<String, InstanceSnapJob>()
    private val snapsToCapture = mutableListOf<InstanceSnapJob>()
    private val pointsToCapture = mutableListOf<PointsJob>()

    fun dispose(entityInstance: EntityInstance) {
        removeCachedImages(entityInstance.id)
    }

    fun instanceSnap(entityInstance: EntityInstance, model: EntityModel, axes: Axes): Deferred<ImageBitmap> {
        val key = cacheKey(entityInstance.id, axes)
        val signature = entityInstance.rotateHash()
        jobs[key]?.let { cached ->
            if (cached.signature == signature) {
                return cached.deferred
            }
            jobs.remove(key)?.let { snapsToCapture.remove(it) }
        }
        println("Request snap image: $key, total snap images: ${jobs.size}")
        removeCachedImage(entityInstance.id, axes)
        return jobs.computeIfAbsent(key) {
            println("Snap image cache miss: $key")
            val job = InstanceSnapJob(entityInstance, model, axes, signature)
            snapsToCapture += job
            job
        }.deferred
    }

    @OptIn(ExperimentalUuidApi::class)
    fun entityPoints(filename: String): Deferred<List<Vec3>> {
        val job = PointsJob(filename)
        pointsToCapture += job
        return job.deferred
    }

    private fun cacheKey(entityInstanceId: String, axes: Axes) = "${entityInstanceId}_${axes.name}"

    private fun removeCachedImages(entityInstanceId: String) {
        jobs.entries.removeAll { (_, job) -> job.instance.id == entityInstanceId }
        snapsToCapture.removeAll { job -> job.instance.id == entityInstanceId }
    }

    private fun removeCachedImage(entityInstanceId: String, axes: Axes) {
        val key = cacheKey(entityInstanceId, axes)
        jobs.remove(key)?.let { snapsToCapture.remove(it) }
    }

    context(fs: FrameScope)
    fun frame() = with(fs) {
        renderSnaps()
        renderPoints()
    }

    private fun FrameScope.renderSnaps() {
        if (snapsToCapture.isNotEmpty())
            println("Snap image job queue has: ${snapsToCapture.size} jobs")
        snapsToCapture.forEach { job ->
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
                    Model(job.model.filename, job.instance.transform)
                }
            }
            deferredKorenderImage.invokeOnCompletion {
                println("Capture snap image DONE: ${job.instance.hashCode()}_${job.axes.name}")
                val imageBitmap = deferredKorenderImage.getCompleted().toCompose()
                job.deferred.complete(imageBitmap)
            }
        }
        snapsToCapture.clear()
    }

    private fun FrameScope.renderPoints() {
        pointsToCapture.forEach { job ->
            println("Capture points: ${job.filename}")
            Node(resourceLoader = { File(it.split("#")[1]).readBytes() }) {
                Model(job.uniqueName, onUpdate = {
                    println("Capture points done: ${job.filename}")
                    job.deferred.complete(collectModelPoints(it))
                    pointsToCapture.remove(job)
                })
            }

        }
    }

}
