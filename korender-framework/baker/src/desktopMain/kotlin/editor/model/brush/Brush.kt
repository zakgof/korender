package editor.model.brush

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.zakgof.korender.math.Vec3
import editor.model.BoundingBox
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Face(
    val plane: Plane,
    val materialId: String,
    val texturing: Texturing = Texturing()
)

@OptIn(ExperimentalUuidApi::class)
data class Brush(
    val name: String,
    val projectionColor: Int,
    val faces: List<Face>,
    val id: String = Uuid.generateV7().toHexDashString()
) {

    constructor(name: String, projectionColor: Color, bb: BoundingBox, materialId: String) :
            this(name, projectionColor.toArgb(), Plane.cube(bb).map { Face(it, materialId) })

    val mesh by lazy { BrushMesher.buildBrushMesh(this) }

    val bb by lazy {
        val vertices = mesh.points
        val minX = vertices.minOf { it.x }
        val minY = vertices.minOf { it.y }
        val minZ = vertices.minOf { it.z }
        val maxX = vertices.maxOf { it.x }
        val maxY = vertices.maxOf { it.y }
        val maxZ = vertices.maxOf { it.z }
        BoundingBox(
            Vec3(minX, minY, minZ),
            Vec3(maxX, maxY, maxZ)
        )
    }

    fun translate(offset: Vec3) =
        copy(faces = faces.map { face -> face.copy(plane = face.plane.translate(offset)) })

    fun scale(oldBB: BoundingBox, newBB: BoundingBox): Brush {
        val scale = (newBB.max - newBB.min) divpercomp (oldBB.max - oldBB.min)

        return copy(faces = faces.map { face ->
            face.copy(plane = face.plane.scale(oldBB, newBB))
        })
    }

    fun rotate(center: Vec3, axis: Vec3, angle: Float) =
        copy(faces = faces.map { face -> face.copy(plane = face.plane.rotate(center, axis, angle)) })

    fun intersectRayBrush(r0: Vec3, look: Vec3): Face? {

        var tEnter = Float.NEGATIVE_INFINITY
        var tExit = Float.POSITIVE_INFINITY
        var enterFace: Face? = null

        for (f in faces) {
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

        return enterFace
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
        val validFaces = faces.filter { face ->
            val plane = face.plane
            val onPlane = verts.count {
                abs(plane.distanceTo(it)) < EPS
            }
            onPlane >= 3
        }
        return if (validFaces.size == faces.size) this else copy(faces = validFaces)
    }

    companion object {
        fun carve(target: Collection<Brush>, by: Brush, materialId: String): Pair<Collection<Brush>, Collection<Brush>> {
            val splitSources = mutableListOf<Brush>()
            val splitResults = mutableListOf<Brush>()
            val planes = by.faces.map { it.plane }
            for (brush in target) {
                brush.carveBy(planes, materialId)?.let {
                    splitSources += brush
                    splitResults += it
                }
            }
            return splitSources to splitResults
        }
    }

    private fun carveBy(by: List<Plane>, materialId: String): List<Brush>? {

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
                        faces = currentPart.faces + Face(plane.invert(), materialId)
                    ).filterFaces()
                    val back = currentPart.copy(
                        name = "$name*",
                        id = Uuid.generateV7().toHexDashString(),
                        faces = currentPart.faces + Face(plane, materialId)
                    ).filterFaces()
                    results += front
                    currentPart = back
                }
            }
        }
        return results
    }


}

