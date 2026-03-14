package editor.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.zakgof.korender.baker.editor.model.Group
import com.zakgof.korender.math.Vec3
import editor.model.brush.Brush
import editor.model.brush.Face
import editor.model.brush.Plane
import editor.model.brush.Texturing
import editor.model.brush.Texturing.Axis
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable

@Serializable
class ModelDto(
    val brushes: List<BrushDto>,
    val invisibleBrushes: Set<String>,
    val groups: List<GroupDto>,
    val materials: List<MaterialDto>,
    val version: Int = 1,
) {
    constructor(model: Model) : this(
        model.brushes.values.map { BrushDto(it) },
        model.invisibleBrushes,
        model.groups.values.map { GroupDto(it) },
        model.materials.values.map { MaterialDto(it) }
    )

    fun toModel() = Model(
        brushes.map { it.toBrush() }.associateBy { it.id }.toPersistentMap(),
        invisibleBrushes,
        groups.map { it.toGroup() }.associateBy { it.id }.toPersistentMap(),
        groups.flatMap { group -> group.brushIds.map { it to group.id } }.toMap().toPersistentMap(),
        materials.map { it.toMaterial() }.associateBy { it.id }.toPersistentMap(),
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

@Serializable
data class PlaneDto(
    val p0: Vec3Dto,
    val pu: Vec3Dto,
    val pv: Vec3Dto,
) {
    constructor(plane: Plane) : this(
        Vec3Dto(plane.p0),
        Vec3Dto(plane.pu),
        Vec3Dto(plane.pv)
    )

    fun toPlane() = Plane(
        p0 = p0.toVec3(),
        pu = pu.toVec3(),
        pv = pv.toVec3()
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
    val projectionColor: Int,
    val planes: List<FaceDto>,
) {
    constructor(brush: Brush) : this(
        name = brush.name,
        projectionColor = brush.projectionColor,
        planes = brush.faces.map { FaceDto(it) },
        id = brush.id
    )

    fun toBrush() = Brush(
        name = name,
        projectionColor = projectionColor,
        faces = planes.map { it.toFace() },
        id = id
    )
}

@Serializable
data class MaterialDto(
    val name: String,
    val colorTexture: TexId? = null,
    val baseColor: Int,
    val id: String,
) {
    constructor(material: Material) : this(
        name = material.name,
        colorTexture = material.colorTexture,
        baseColor = material.baseColor.toArgb(),
        id = material.id
    )

    fun toMaterial() = Material(
        name = name,
        colorTexture = colorTexture,
        baseColor = Color(baseColor),
        id = id
    )
}
