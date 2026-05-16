package editor.model.entity

import com.zakgof.korender.baker.editor.model.Boundable
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class EntityInstance(
    val name: String,
    val modelId: String,
    val points: List<Vec3>,
    val transform: Transform,
    val id: String = Uuid.generateV7().toHexDashString(),
) : Boundable {
    companion object {
        private const val HASH_TOLERANCE = 0.001f
    }

    constructor(name: String, model: EntityModel, transform: Transform) :
            this(name, model.id, model.points, transform)

    override val bb by lazy { BoundingBox.from(points.map { transform * it }) }

    fun rotateHash(): String {
        val rotationSignature = transform.mat4.rotationSignature()

        return buildString {
            append(modelId)
            append('_')
            append(rotationSignature)
        }
    }

    private fun com.zakgof.korender.math.Mat4.rotationSignature(): String {
        val c0 = Vec3(m00, m10, m20).normalizedOrZero()
        val c1 = Vec3(m01, m11, m21).normalizedOrZero()
        val c2 = Vec3(m02, m12, m22).normalizedOrZero()
        return listOf(c0, c1, c2)
            .flatMap { listOf(it.x, it.y, it.z) }
            .joinToString(",") { it.quantizedHashBucket().toString() }
    }

    private fun Vec3.normalizedOrZero(): Vec3 {
        val len = length()
        return if (len > 0f) this * (1f / len) else Vec3.ZERO
    }

    private fun Float.quantizedHashBucket(): Int {
        return (this / HASH_TOLERANCE).roundToInt()
    }
}


