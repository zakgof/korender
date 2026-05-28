package com.zakgof.korender.impl.model.obj

import com.zakgof.korender.KorenderException
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.InternalModelInstancingDeclaration
import com.zakgof.korender.impl.engine.MeshInstance
import com.zakgof.korender.impl.engine.ModelDeclaration
import com.zakgof.korender.impl.engine.RenderableDeclaration
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.geometry.CMesh
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalInstancingParameter
import com.zakgof.korender.impl.geometry.MeshAttributes.NORMAL
import com.zakgof.korender.impl.geometry.MeshAttributes.POS
import com.zakgof.korender.impl.geometry.MeshAttributes.TEX
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.model.InternalModel
import com.zakgof.korender.impl.model.InternalModelInfo
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

@OptIn(ExperimentalCoroutinesApi::class)
internal class ObjScene(declaration: ModelDeclaration) : InternalModel {

    private val prefix = "obj[${declaration.resource}]"
    private var loadNotified = false

    private val sceneDeferred = CoroutineScope(Dispatchers.Default).async {
        val objBytes = declaration.nodeContext.resourceLoader(declaration.resource)
        val parsed = ObjSceneLoader.load(declaration.resource, objBytes)
        val materialMap = parsed.materialLibraries
            .flatMap { materialResource ->
                ObjSceneLoader.loadMaterials(
                    materialResource,
                    declaration.nodeContext.resourceLoader(materialResource)
                )
            }
            .associateBy { it.name }
        val preparedMeshes = parsed.meshes.map {
            PreparedMesh(it.name, toCMesh(it, declaration.instancingDeclaration), materialMap[it.materialName])
        }
        LoadedObjScene(preparedMeshes)
    }

    private fun toCMesh(mesh: ObjSceneMesh, instancingDeclaration: InternalModelInstancingDeclaration?): CMesh {
        val instancingAttributes = instancingDeclaration?.let {InternalInstancingParameter.TRANSFORM_INSTANCING.instanceMeshAttributes.toTypedArray()} ?: arrayOf()
        return CMesh(
            vertexCount = mesh.vertices.size,
            indexCount = mesh.indices.size,
            instanceCount = instancingDeclaration?.count ?: -1,
            POS, NORMAL, TEX, *instancingAttributes
        ) {
            mesh.vertices.forEach {
                pos(it.pos).normal(it.normal).tex(it.tex)
            }
            index(*mesh.indices.toIntArray())
        }
    }


    override fun build(modelDeclaration: ModelDeclaration, sceneDeclaration: SceneDeclaration, rk: ResultKeeper?) {

        if (!sceneDeferred.isCompleted) {
            rk?.fail()
            return
        }

        val scene = sceneDeferred.getCompleted()
        if (!loadNotified) {
            loadNotified = true
            modelDeclaration.onUpdate?.invoke(scene.modelInfo())
        }
        scene.meshes.forEachIndexed { index, preparedMesh ->
            val material = InternalBaseMaterial().apply {
                preparedMesh.material?.applyTo(this, modelDeclaration.nodeContext)
                modelDeclaration.materialModifier(this)
            }
            val singleMeshDeclaration = modelDeclaration.nodeContext.mesh(
                id = "$prefix.mesh.$index.${preparedMesh.name}",
                mesh = preparedMesh.cmesh
            )

            // TODO transparency support
            val meshDeclaration = modelDeclaration.instancingDeclaration?.let { instancing ->
                InstancedMesh(
                    instancing.id,
                    instancing.count,
                    singleMeshDeclaration,
                    !instancing.dynamic,
                    false,
                    modelDeclaration.nodeContext,
                    listOf(InternalInstancingParameter.TRANSFORM_INSTANCING),
                ) {
                    instancing.instancer.invoke().map { modelInstance ->
                        MeshInstance(transform = modelInstance.transform)
                    }
                }
            } ?: singleMeshDeclaration

            sceneDeclaration.append(
                RenderableDeclaration(
                    material = material,
                    mesh = meshDeclaration,
                    transform = modelDeclaration.transform,
                    transparent = material.color.a < 1f,
                    nodeContext = modelDeclaration.nodeContext,
                )
            )
        }
    }
}

private data class LoadedObjScene(val meshes: List<PreparedMesh>) {
    fun modelInfo() : InternalModelInfo {
        val renderables = meshes.map {
            InternalModelInfo.Renderable(null, it.cmesh, it.material?.toMaterialInfo())
        }
        val instance = InternalModelInfo.Node("instances", null, null, renderables)
        return InternalModelInfo(
            listOf(instance),
            null,
            null
        )
    }
}

private data class PreparedMesh(
    val name: String?,
    val cmesh: CMesh,
    val material: ObjSceneMaterial?
)

private data class ObjSceneMesh(
    val name: String,
    val materialName: String?,
    val vertices: List<ObjSceneVertex>,
    val indices: List<Int>,
)

private data class ObjSceneVertex(
    override val pos: Vec3,
    override val normal: Vec3,
    override val tex: Vec2,
) : Mesh.Vertex {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(attribute: MeshAttribute<T>): T? =
        when (attribute) {
            POS -> pos
            NORMAL -> normal
            TEX -> tex
            else -> null
        } as T?
}

private data class ObjSceneMaterial(
    val name: String,
    val color: ColorRGBA = ColorRGBA.White,
    val colorTexture: String? = null,
    val metallic: Float? = null,
    val roughness: Float? = null,
) {
    fun applyTo(material: InternalBaseMaterial, nodeContext: NodeContext) {
        material.color = color
        colorTexture?.let {
            material.colorTexture = nodeContext.texture(it)
        }
        metallic?.let {
            material.metallicFactor = it
        }
        roughness?.let {
            material.roughnessFactor = it
        }
    }

    fun toMaterialInfo() = InternalModelInfo.Material (
        name = name,
        color = color,
        colorTextureResource = colorTexture,
        metallicFactor = metallic ?: 0.1f,
        roughnessFactor = roughness ?: 0.5f
    )
}

private object ObjSceneLoader {

    fun load(resource: String, bytes: ByteArray): ParsedObjScene {
        val base = resourceParent(resource)
        val positions = mutableListOf<Vec3>()
        val normals = mutableListOf<Vec3>()
        val texes = mutableListOf<Vec2>()
        val materialLibraries = mutableListOf<String>()
        val meshes = mutableListOf<MeshBuilder>()
        var meshName = "default"
        var materialName: String? = null

        fun mesh() = meshes.lastOrNull { it.name == meshName && it.materialName == materialName }
            ?: MeshBuilder(meshName, materialName).also { meshes += it }

        bytes.decodeToString().lines().forEach { rawLine ->
            val line = rawLine.substringBefore("#").trim()
            if (line.isBlank())
                return@forEach

            val command = line.split(Regex("\\s+"), limit = 2)
            val header = command[0]
            val body = command.getOrElse(1) { "" }.trim()

            when (header) {
                "mtllib" -> materialLibraries += body.split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .map { base + it.replace('\\', '/') }

                "o", "g" -> meshName = body.ifBlank { "default" }
                "usemtl" -> materialName = body.ifBlank { null }
                "v" -> positions += parse3(body, "Obj v expects 3 coordinates")
                "vn" -> normals += parse3(body, "Obj vn expects 3 coordinates")
                "vt" -> texes += parse2(body)
                "f" -> addFace(mesh(), body, positions, normals, texes)
            }
        }

        return ParsedObjScene(
            materialLibraries,
            meshes.map { it.toMesh() }.filter { it.indices.isNotEmpty() }
        )
    }

    fun loadMaterials(resource: String, bytes: ByteArray): List<ObjSceneMaterial> {
        val base = resourceParent(resource)
        val materials = mutableListOf<MaterialBuilder>()
        fun current() = materials.lastOrNull() ?: throw KorenderException("Mtl property before newmtl in $resource")

        bytes.decodeToString().lines().forEach { rawLine ->
            val line = rawLine.substringBefore("#").trim()
            if (line.isBlank())
                return@forEach

            val command = line.split(Regex("\\s+"), limit = 2)
            val header = command[0]
            val body = command.getOrElse(1) { "" }.trim()

            when (header) {
                "newmtl" -> materials += MaterialBuilder(body)
                "Kd" -> current().diffuse = parse3(body, "Mtl Kd expects 3 coordinates")
                "d" -> current().alpha = body.toFloat()
                "Tr" -> current().alpha = 1f - body.toFloat()
                "map_Kd" -> current().colorTexture = base + textureName(body).replace('\\', '/')
                "Pm" -> current().metallic = body.toFloat()
                "Pr" -> current().roughness = body.toFloat()
                "Ns" -> current().roughness = 1f - (body.toFloat().coerceIn(0f, 1000f) / 1000f)
            }
        }

        return materials.map { it.toMaterial() }
    }

    private fun addFace(
        mesh: MeshBuilder,
        body: String,
        positions: List<Vec3>,
        normals: List<Vec3>,
        texes: List<Vec2>,
    ) {
        val refs = body.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { parseVertexRef(it, positions.size, texes.size, normals.size) }

        if (refs.size != 3 && refs.size != 4)
            throw KorenderException("Obj f expects 3 or 4 vertices")

        val faceNormal = calculateFaceNormal(refs, positions)
        val faceIndices = refs.map { mesh.addVertex(it, faceNormal, positions, normals, texes) }
        mesh.indices += listOf(faceIndices[0], faceIndices[1], faceIndices[2])
        if (faceIndices.size == 4)
            mesh.indices += listOf(faceIndices[0], faceIndices[2], faceIndices[3])
    }

    private fun parseVertexRef(token: String, positionCount: Int, texCount: Int, normalCount: Int): VertexRef {
        val parts = token.split("/")
        return VertexRef(
            position = parseIndex(parts[0], positionCount, "position"),
            tex = parts.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { parseIndex(it, texCount, "texcoord") },
            normal = parts.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { parseIndex(it, normalCount, "normal") },
        )
    }

    private fun parseIndex(value: String, count: Int, label: String): Int {
        val index = value.toInt()
        val resolved = if (index > 0) index - 1 else count + index
        if (resolved !in 0..<count)
            throw KorenderException("Obj $label index $index out of range")
        return resolved
    }

    private fun calculateFaceNormal(refs: List<VertexRef>, positions: List<Vec3>): Vec3 {
        val a = positions[refs[0].position]
        val b = positions[refs[1].position]
        val c = positions[refs[2].position]
        val normal = (b - a) % (c - a)
        return if (normal.lengthSquared() > 0f) normal.normalize() else Vec3.Y
    }

    private fun parse3(line: String, message: String): Vec3 {
        val tokens = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size < 3)
            throw KorenderException(message)
        return Vec3(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat())
    }

    private fun parse2(line: String): Vec2 {
        val tokens = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size < 2)
            throw KorenderException("Obj vt expects 2 coordinates")
        return Vec2(tokens[0].toFloat(), 1f - tokens[1].toFloat())
    }

    private fun textureName(line: String): String =
        line.split(Regex("\\s+")).last { it.isNotBlank() }

    private fun resourceParent(resource: String): String {
        val slash = maxOf(resource.lastIndexOf('/'), resource.lastIndexOf('\\'))
        return if (slash < 0) "" else resource.substring(0, slash + 1).replace('\\', '/')
    }
}

private data class ParsedObjScene(
    val materialLibraries: List<String>,
    val meshes: List<ObjSceneMesh>,
)

private data class VertexRef(
    val position: Int,
    val tex: Int?,
    val normal: Int?,
)

private class MeshBuilder(
    val name: String,
    val materialName: String?,
) {
    private val vertices = mutableListOf<ObjSceneVertex>()
    private val vertexMap = mutableMapOf<VertexRef, Int>()
    val indices = mutableListOf<Int>()

    fun addVertex(
        ref: VertexRef,
        faceNormal: Vec3,
        positions: List<Vec3>,
        normals: List<Vec3>,
        texes: List<Vec2>,
    ): Int {
        if (ref.normal == null)
            return appendVertex(ref, faceNormal, positions, texes)

        return vertexMap.getOrPut(ref) {
            appendVertex(ref, normals[ref.normal], positions, texes)
        }
    }

    private fun appendVertex(
        ref: VertexRef,
        normal: Vec3,
        positions: List<Vec3>,
        texes: List<Vec2>,
    ): Int {
        vertices += ObjSceneVertex(
            pos = positions[ref.position],
            normal = normal,
            tex = ref.tex?.let { texes[it] } ?: Vec2.ZERO,
        )
        return vertices.size - 1
    }

    fun toMesh() = ObjSceneMesh(name, materialName, vertices, indices)
}

private class MaterialBuilder(val name: String) {
    var diffuse = Vec3(1f, 1f, 1f)
    var alpha = 1f
    var colorTexture: String? = null
    var metallic: Float? = null
    var roughness: Float? = null

    fun toMaterial() = ObjSceneMaterial(
        name = name,
        color = ColorRGBA(diffuse.x, diffuse.y, diffuse.z, alpha),
        colorTexture = colorTexture,
        metallic = metallic,
        roughness = roughness,
    )
}
