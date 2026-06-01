package editor.model.brush

import com.zakgof.korender.baker.editor.model.Boundable
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Face(
    val plane: Plane,
    val materialId: String,
    val texturing: Texturing,
    val id: String = Uuid.generateV7().toHexDashString(),
) {
    constructor(plane: Plane, materialId: String, fitToFace: Boolean) :
            this(plane, materialId, Texturing(fitToFace = fitToFace))
}

@OptIn(ExperimentalUuidApi::class)
data class Brush(
    val name: String,
    val faces: Map<String, Face>,
    val id: String = Uuid.generateV7().toHexDashString(),
) : Boundable {

    class Point(val pos: Vec3, val planes: List<Plane>)

    constructor(
        name: String,
        bb: BoundingBox,
        shape: CreatorShape,
        materialId: String,
        fitToFace: Boolean,
    ) : this(name, shape.makeFaces(bb, materialId, fitToFace).associateBy { it.id })

    val points by lazy { BrushMesher.collectPoints(this) }
    val mesh by lazy { BrushMesher.buildBrushMesh(this, points) }

    override val bb by lazy {
        val vertices = points
        val minX = vertices.minOf { it.pos.x }
        val minY = vertices.minOf { it.pos.y }
        val minZ = vertices.minOf { it.pos.z }
        val maxX = vertices.maxOf { it.pos.x }
        val maxY = vertices.maxOf { it.pos.y }
        val maxZ = vertices.maxOf { it.pos.z }
        BoundingBox(
            Vec3(minX, minY, minZ),
            Vec3(maxX, maxY, maxZ)
        )
    }

    fun translate(offset: Vec3) =
        copy(faces = faces.values.map { face ->
            face.copy(plane = face.plane.translate(offset))
        }.associateBy { it.id })

    fun scale(oldBB: BoundingBox, newBB: BoundingBox) =
        copy(faces = faces.values.map { face ->
            face.copy(plane = face.plane.scale(oldBB, newBB))
        }.associateBy { it.id })

    fun rotate(center: Vec3, axis: Vec3, angle: Float) =
        copy(faces = faces.values.map { face ->
            face.copy(plane = face.plane.rotate(center, axis, angle))
        }.associateBy { it.id })

    fun intersectRayBrush(r0: Vec3, look: Vec3): Pair<Face, Vec3>? {

        var tEnter = Float.NEGATIVE_INFINITY
        var tExit = Float.POSITIVE_INFINITY
        var enterFace: Face? = null

        for (f in faces.values) {
            val p = f.plane
            val dist0 = p.normal * r0 + p.d
            val denom = p.normal * look

            if (abs(denom) < 1e-6f) {
                if (dist0 > 0f) return null
                continue
            }
            val t = -dist0 / denom
            if (denom < 0f) {
                if (t > tEnter) {
                    tEnter = t
                    enterFace = f
                }
            } else {
                if (t < tExit) tExit = t
            }
            if (tEnter > tExit) return null
        }
        if (tExit < 0f) return null

        return enterFace?.let { it to (r0 + look * tEnter) }
    }

    enum class PlaneSide {
        FRONT, BACK, SPLIT
    }

    fun classifyPlane(p: Plane): PlaneSide {
        val EPS = 1e-3 // TODO unify EPSes
        val verts = mesh.points
        if (verts.isEmpty()) return PlaneSide.BACK

        var hasFront = false
        var hasBack = false

        for (v in verts) {
            val dist = p.distanceTo(v)
            if (dist > EPS) hasFront = true
            if (dist < -EPS) hasBack = true

            if (hasFront && hasBack)
                return PlaneSide.SPLIT
        }

        return when {
            hasFront -> PlaneSide.FRONT
            hasBack -> PlaneSide.BACK
            else -> PlaneSide.BACK
        }
    }

    private fun filterFaces(): Brush {
        val EPS = 1e-3f
        val verts = mesh.points
        val validFaces = faces.values.filter { face ->
            val plane = face.plane
            val onPlane = verts.count {
                abs(plane.distanceTo(it)) < EPS
            }
            onPlane >= 3
        }
        return if (validFaces.size == faces.size) this else copy(faces = validFaces.associateBy { it.id })
    }

    companion object {
        fun carve(target: Collection<Brush>, by: Set<Brush>, materialId: String, fitToFace: Boolean): Set<Pair<Brush, Collection<Brush>>> {
            if (by.isEmpty()) return emptySet()

            val originals = target.toList()
            val partsMap = originals.associate { it.id to mutableListOf(it) }.toMutableMap()

            for (tool in by) {
                val planes = tool.faces.values.map { it.plane }
                if (planes.isEmpty()) continue

                val newPartsMap = mutableMapOf<String, MutableList<Brush>>()

                for ((origId, parts) in partsMap) {
                    val accum = mutableListOf<Brush>()
                    for (part in parts) {
                        val carved = part.carveBy(planes, materialId, fitToFace)
                        if (carved == null) accum += part else accum += carved
                    }
                    newPartsMap[origId] = accum
                }

                partsMap.clear()
                partsMap.putAll(newPartsMap)
            }

            val result = mutableSetOf<Pair<Brush, Collection<Brush>>>()
            for (orig in originals) {
                val finalParts = partsMap[orig.id] ?: listOf(orig)
                if (!(finalParts.size == 1 && finalParts[0].id == orig.id)) {
                    result += orig to finalParts
                }
            }

            return result
        }
    }

    private fun carveBy(by: List<Plane>, materialId: String, fitToFace: Boolean): List<Brush>? {

        val results = mutableListOf<Brush>()
        var currentPart = this

        for (plane in by) {

            when (currentPart.classifyPlane(plane)) {
                PlaneSide.FRONT -> {
                    return null
                }

                PlaneSide.BACK -> {
                }

                PlaneSide.SPLIT -> {
                    val front = currentPart.copy(
                        name = "$name*",
                        id = Uuid.generateV7().toHexDashString(),
                        faces = (currentPart.faces.values + Face(plane.invert().copy(smoothId = Uuid.generateV7()), materialId, fitToFace)).associateBy { it.id }
                    ).filterFaces()
                    val back = currentPart.copy(
                        name = "$name*",
                        id = Uuid.generateV7().toHexDashString(),
                        faces = (currentPart.faces.values  + Face(plane.copy(smoothId = Uuid.generateV7()), materialId, fitToFace)).associateBy { it.id }
                    ).filterFaces()
                    results += front
                    currentPart = back
                }
            }
        }
        return results
    }


}

