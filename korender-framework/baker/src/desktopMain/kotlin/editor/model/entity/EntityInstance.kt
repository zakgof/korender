package editor.model.entity

import com.zakgof.korender.baker.editor.model.Boundable
import com.zakgof.korender.math.Mat4
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

    fun rotateHash() = buildString {
        append(modelId)
        append('_')
        append(transform.mat4.rotationSignature())
    }

    private fun Mat4.rotationSignature(): String {
        val r0 = Vec3(m00, m01, m02).normalizedOrZero()
        val r1 = Vec3(m10, m11, m12).normalizedOrZero()
        val r2 = Vec3(m20, m21, m22).normalizedOrZero()
        return listOf(r0, r1, r2)
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
