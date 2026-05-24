package editor.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import editor.model.brush.Brush
import editor.model.brush.Face
import editor.model.brush.Group
import editor.model.brush.Plane
import editor.model.brush.Texturing
import editor.model.brush.Texturing.Axis
import editor.model.entity.EntityInstance
import editor.model.entity.EntityModel
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
class ModelDto(
    val brushes: List<BrushDto>,
    val invisibleBrushes: Set<String>,
    val groups: List<GroupDto>,
    val materials: List<MaterialDto>,
    val entityModels: List<EntityModelDto> = emptyList(),
    val entityInstances: List<EntityInstanceDto> = emptyList(),
    val version: Int = 2,
) {
    constructor(model: Model) : this(
        model.brushes.values.map { BrushDto(it) },
        model.invisibleBrushes,
        model.groups.values.map { GroupDto(it) },
        model.materials.values.map { MaterialDto(it) },
        model.entityModels.values.map { EntityModelDto(it) },
        model.entityInstances.values.map { EntityInstanceDto(it) }
    )

    fun toModel() = Model(
        brushes.map { it.toBrush() }.associateBy { it.id }.toPersistentMap(),
        invisibleBrushes,
        groups.map { it.toGroup() }.associateBy { it.id }.toPersistentMap(),
        groups.flatMap { group -> group.brushIds.map { it to group.id } }.toMap().toPersistentMap(),
        materials.map { it.toMaterial() }.associateBy { it.id }.toPersistentMap(),
        entityInstances.map { it.toEntityInstance() }.associateBy { it.id }.toPersistentMap(),
        entityModels.map { it.toEntityModel() }.associateBy { it.id }.toPersistentMap(),
    )
}

@Serializable
data class Vec3Dto(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    constructor(vec: Vec3) : this(vec.x, vec.y, vec.z)

    fun toVec3() = Vec3(x, y, z)
}

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PlaneDto(
    val p0: Vec3Dto,
    val pu: Vec3Dto,
    val pv: Vec3Dto,
    val smoothId: Uuid
) {
    constructor(plane: Plane) : this(
        Vec3Dto(plane.p0),
        Vec3Dto(plane.pu),
        Vec3Dto(plane.pv),
        plane.smoothId
    )

    fun toPlane() = Plane(
        p0 = p0.toVec3(),
        pu = pu.toVec3(),
        pv = pv.toVec3(),
        smoothId = smoothId
    )
}

@Serializable
data class FaceDto(
    val plane: PlaneDto,
    val materialId: String,
    val texturing: TexturingDto,
) {
    constructor(face: Face) : this(PlaneDto(face.plane), face.materialId, TexturingDto(face.texturing))

    fun toFace() = Face(
        plane = plane.toPlane(),
        materialId = materialId,
        texturing = texturing.toTexturing()
    )
}

@Serializable
data class TexturingDto(
    val uScale: Float,
    val uOffset: Float,
    val vScale: Float,
    val vOffset: Float,
    val fitToFace: Boolean,
) {
    constructor(texturing: Texturing)
            : this(texturing.u.scale, texturing.u.offset, texturing.v.scale, texturing.v.offset, texturing.fitToFace)

    fun toTexturing() = Texturing(
        Axis(uScale, uOffset),
        Axis(vScale, vOffset),
        fitToFace
    )
}

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val brushIds: Set<String>,
) {
    constructor(group: Group) : this(
        id = group.id,
        name = group.name,
        brushIds = group.brushIds,
    )

    fun toGroup() = Group(name, brushIds, id)
}


@Serializable
data class BrushDto(
    val id: String,
    val name: String,
    val planes: List<FaceDto>,
) {
    constructor(brush: Brush) : this(
        name = brush.name,
        planes = brush.faces.map { FaceDto(it) },
        id = brush.id
    )

    fun toBrush() = Brush(
        name = name,
        faces = planes.map { it.toFace() },
        id = id
    )
}

@Serializable
data class MaterialDto(
    val name: String,
    val colorTexture: String? = null,
    val baseColor: Int,
    val id: String,
    val fitToFace: Boolean,
    val stochastic: Boolean,
    val triplanar: Boolean,
    val scale: Float,
    val metallic: Float,
    val roughness: Float
) {
    constructor(material: Material) : this(
        name = material.name,
        colorTexture = material.colorTexture,
        baseColor = material.baseColor.toArgb(),
        id = material.id,
        fitToFace = material.fitToFace,
        stochastic = material.stochastic,
        triplanar = material.triplanar,
        scale = material.scale,
        metallic = material.metallic,
        roughness = material.roughness
    )

    fun toMaterial() = Material(
        name = name,
        colorTexture = colorTexture,
        baseColor = Color(baseColor),
        id = id,
        fitToFace = fitToFace,
        stochastic = stochastic,
        triplanar = triplanar,
        scale = scale,
        metallic = metallic,
        roughness = roughness
    )
}

@Serializable
data class TransformDto(
    val matrix: FloatArray = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    ),
) {
    constructor(transform: Transform) : this(transform.mat4.asArray())

    fun toTransform() = Transform(com.zakgof.korender.math.Mat4(matrix))
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TransformDto
        return matrix.contentEquals(other.matrix)
    }

    override fun hashCode(): Int {
        return matrix.contentHashCode()
    }
}

@Serializable
data class EntityModelDto(
    val id: String,
    val name: String,
    val filename: String,
    val defaultScale: Float,
    val points: List<Vec3Dto> = emptyList(),
) {
    constructor(entityModel: EntityModel) : this(
        id = entityModel.id,
        name = entityModel.name,
        filename = entityModel.filename,
        defaultScale = entityModel.defaultScale,
        points = entityModel.points.map { Vec3Dto(it) },
    )

    fun toEntityModel() = EntityModel(
        name = name,
        filename = filename,
        defaultScale = defaultScale,
        points = points.map { it.toVec3() },
        id = id,
    )

}

@Serializable
data class EntityInstanceDto(
    val id: String,
    val name: String,
    val modelId: String,
    val points: List<Vec3Dto> = emptyList(),
    val transform: TransformDto = TransformDto(),
) {
    constructor(entityInstance: EntityInstance) : this(
        id = entityInstance.id,
        name = entityInstance.name,
        modelId = entityInstance.modelId,
        points = entityInstance.points.map { Vec3Dto(it) },
        transform = TransformDto(entityInstance.transform),
    )

    fun toEntityInstance() = EntityInstance(
        name = name,
        modelId = modelId,
        points = points.map { it.toVec3() },
        transform = transform.toTransform(),
        id = id,
    )
}
