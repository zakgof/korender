package editor.util

import com.zakgof.korender.ByteArrayTextureDeclaration
import com.zakgof.korender.KorenderException
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.ResourceTextureDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.impl.buffer.NativeBuffer
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.NativeFloatBuffer
import com.zakgof.korender.impl.buffer.toByteArray
import com.zakgof.korender.impl.scene.KrModel
import com.zakgof.korender.impl.scene.KrModel.Attribute
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import editor.cache.KorenderCache
import editor.cache.TextureImageCache
import editor.model.Material
import editor.model.Model
import editor.model.brush.BrushMesh
import editor.model.brush.Face
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ModelCompiler {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun compile(model: Model): KrModel {

        val entityRenderables = model.entityInstances.values.flatMap { entityInstance ->
            val entityModel = model.entityModels[entityInstance.modelId]!!
            val modelInfo = KorenderCache.entityModelInfo(entityModel.filename)
            modelInfo.renderables(entityInstance.transform)
        }

        val entityMaterialToIdMap = entityRenderables.mapNotNull { it.first.material }.toSet()
            .associateWith { "em-" + Uuid.random() }

        val entityTextureToIdMap = entityMaterialToIdMap.keys.mapNotNull { it.colorTextureResource }.toSet()
            .associateWith { "et-" + Uuid.random() }

        val eTextures = entityTextureToIdMap.entries.map { it.key.toKrTexture(it.value) }
            .associateBy{ it.id }
        val eMaterials = entityMaterialToIdMap.entries.map { it.key.toKrMaterial(it.value, entityTextureToIdMap[it.key.colorTextureResource]) }
            .associateBy { it.id }
        val eMeshes = entityRenderables.mapIndexed { index, pair ->
            val mesh = pair.first.mesh
            KrModel.Mesh(
                "emesh-$index",
                mesh.vertices.size,
                mesh.indices?.size ?: 0,
                mesh.attrBytes(),
                mesh.indexBytes()
            )
        }
        val eRenderables = entityRenderables.mapIndexed { index, pair ->
            KrModel.Renderable(
                "er-$index",
                "emesh-$index",
                entityMaterialToIdMap[pair.first.material]!!,
                pair.second.mat4.asArray()
            )
        }

        /////

        val usedMaterialIds = model.brushes.values
            .flatMap { it.faces }
            .map { it.materialId }
            .toSet()
        val usedMaterials = model.materials.values
            .filter { usedMaterialIds.contains(it.id) }

        val textures = usedMaterials
            .filter { it.colorTexture != null }
            .map { it.colorTexture!! }
            .distinct()
            .associateWith { texture(it) }

        val texArrayGroups = usedMaterials
            .filter { it.colorTexture != null }
            .groupBy({ tag(it) }, { it.id to it.colorTexture!! })

        val matIdGroupies = texArrayGroups
            .flatMap { tag -> tag.value.mapIndexed { index, idToPath -> idToPath.first to (tag.key to index) } }
            .toMap()

        val texArrayMaterials = texArrayGroups.map {
            it.key to KrModel.Material(
                id = it.key.toString(),
                baseColor = 0xFFFFFFFF,
                colorTextureId = null,
                colorTextureIds = it.value.map { p -> p.second },
                stochasticSharpness = if (it.key.stochastic) 12f else null,
                triplanarScale = it.key.triplanarScale
            )
        }.toMap()

        val materials = texArrayMaterials.ifEmpty { mapOf("notex" to KrModel.Material("notex")) }
        val noTexMaterialId = materials.keys.first()

        val meshFaces = model.brushes.values.flatMap { brush -> brush.faces.map { brush.mesh to it } }

        val meshes = meshFaces
            .groupBy({
                matIdGroupies[it.second.materialId]?.first ?: noTexMaterialId
            }, {
                it to (matIdGroupies[it.second.materialId]?.second ?: 255)
            })
            .map { matToMeshFacesWithId ->
                val faces = matToMeshFacesWithId.value.map { it.first }
                KrModel.Mesh(
                    matToMeshFacesWithId.key.toString(),
                    matToMeshFacesWithId.value.sumOf { it.first.first.faces[it.first.second]!!.size * 3 },
                    0,
                    mapOf(
                        Attribute.POS to posBytes(faces),
                        Attribute.NORMAL to normalBytes(faces),
                        Attribute.TEX to texBytes(faces),
                        Attribute.COLOR to colorBytes(model.materials, matToMeshFacesWithId.value),
                        Attribute.COLORTEXINDEX to colorTexIndex(matToMeshFacesWithId.value),
                        Attribute.METALLIC to metallicBytes(model.materials, matToMeshFacesWithId.value),
                        Attribute.ROUGHNESS to roughnessBytes(model.materials, matToMeshFacesWithId.value),
                    ),
                    null
                )
            }

        return KrModel(
            textures = textures + eTextures,
            materials = materials.mapKeys { it.key.toString() } + eMaterials,
            meshes = meshes.associateBy { it.id } + eMeshes.associateBy { it.id },
            renderables = meshes.map {
                KrModel.Renderable(
                    it.id,
                    it.id,
                    it.id,
                    Mat4.IDENTITY.asArray()
                )
            }.associateBy { it.id } + eRenderables.associateBy { it.id }
        )
    }

    private fun texture(path: String): KrModel.Texture {
        val file = File(path)
        val image = file.readBytes()
        return KrModel.Texture(file.name, file.extension, image)
    }

    private fun posBytes(faces: List<Pair<BrushMesh, Face>>): ByteArray {
        val poses = faces
            .flatMap { it.first.faces[it.second]!! }
            .flatMap { it.points }
        val nbb = NativeFloatBuffer(poses.size * 3)
        poses.forEach {
            nbb.put(it.x)
            nbb.put(it.y)
            nbb.put(it.z)
        }
        return bytes(nbb)
    }

    private fun normalBytes(faces: List<Pair<BrushMesh, Face>>): ByteArray {
        val normals = faces
            .flatMap { it.first.faces[it.second]!! }
            .map { it.normals }
        val nbb = NativeFloatBuffer(normals.size * 3 * 3)
        normals.forEach { p ->
            p.forEach { n ->
                nbb.put(n.x)
                nbb.put(n.y)
                nbb.put(n.z)
            }
        }
        return bytes(nbb)
    }

    private fun colorTexIndex(faces: List<Pair<Pair<BrushMesh, Face>, Int>>): ByteArray {
        val index = faces
            .flatMap { pair -> List(pair.first.first.faces[pair.first.second]!!.size * 3) { pair.second } }
        val nbb = NativeByteBuffer(index.size * 1)
        index.forEach {
            nbb.put(it.toUByte().toByte())
        }
        return nbb.toByteArray()
    }

    private fun colorBytes(materials: Map<String, Material>, faces: List<Pair<Pair<BrushMesh, Face>, Int>>): ByteArray {
        val colors = faces
            .flatMap { pair -> List(pair.first.first.faces[pair.first.second]!!.size * 3) { materials[pair.first.second.materialId]!!.baseColor.toKorender() } }
        val nbb = NativeByteBuffer(colors.size * 16)
        colors.forEach {
            nbb.put(it.r)
            nbb.put(it.g)
            nbb.put(it.b)
            nbb.put(it.a)
        }
        return nbb.toByteArray()
    }

    private fun metallicBytes(materials: Map<String, Material>, faces: List<Pair<Pair<BrushMesh, Face>, Int>>): ByteArray {
        val metallics = faces
            .flatMap { pair -> List(pair.first.first.faces[pair.first.second]!!.size * 3) { materials[pair.first.second.materialId]!!.metallic } }
        val nbb = NativeByteBuffer(metallics.size * 4)
        metallics.forEach {
            nbb.put(it)
        }
        return nbb.toByteArray()
    }

    private fun roughnessBytes(materials: Map<String, Material>, faces: List<Pair<Pair<BrushMesh, Face>, Int>>): ByteArray {
        val roughness = faces
            .flatMap { pair -> List(pair.first.first.faces[pair.first.second]!!.size * 3) { materials[pair.first.second.materialId]!!.roughness } }
        val nbb = NativeByteBuffer(roughness.size * 4)
        roughness.forEach {
            nbb.put(it)
        }
        return nbb.toByteArray()
    }


    private fun texBytes(faces: List<Pair<BrushMesh, Face>>): ByteArray {
        val texes = faces
            .flatMap { it.first.faces[it.second]!! }
            .flatMap { it.tex }
        val nbb = NativeFloatBuffer(texes.size * 2)
        texes.forEach {
            nbb.put(it.x)
            nbb.put(it.y)
        }
        return bytes(nbb)
    }

    private fun bytes(nbb: NativeBuffer): ByteArray {
        nbb.byteBuffer.rewind()
        val bytes = ByteArray(nbb.byteBuffer.remaining())
        nbb.byteBuffer.get(bytes)
        return bytes
    }

    private fun tag(material: Material): Tag {
        val img = TextureImageCache.compose(material.colorTexture!!)
        return Tag(img.width, img.height, material.stochastic, if (material.triplanar) material.scale else null)
    }

    data class Tag(
        val width: Int,
        val height: Int,
        val stochastic: Boolean,
        val triplanarScale: Float?,
    )

    private fun ModelInfo.Node.renderables(transform: Transform): List<Pair<ModelInfo.Renderable, Transform>> {
        val childTransform = transform * (this.transform ?: Transform.IDENTITY)
        return (this.renderables?.map { it to transform } ?: listOf()) +
                (this.children?.flatMap { it.renderables(childTransform) } ?: listOf())
    }

    private fun ModelInfo.renderables(transform: Transform) = this.instances.flatMap { it.renderables(transform) }

    private fun TextureDeclaration.toKrTexture(id: String): KrModel.Texture = when (this) {
        is ResourceTextureDeclaration -> {
            val file = File(this.textureResource.split("#")[1])
            KrModel.Texture(id, file.extension, file.readBytes())
        }
        is ByteArrayTextureDeclaration -> KrModel.Texture(id, this.extension, this.fileBytesLoader())
        else -> throw KorenderException("Unsupported TextureDeclaration")
    }

    private fun ModelInfo.Material.toKrMaterial(id: String, texId: String?) = KrModel.Material(
        id = id,
        baseColor = color.toLong(),
        colorTextureId = texId,
        metallic = metallicFactor,
        roughness = roughnessFactor
    )

    private fun <T> MeshAttribute<T>.toKrAttribute() =
        when (name) {
            "pos" -> KrModel.Attribute.POS
            "normal" -> KrModel.Attribute.NORMAL
            "tex" -> KrModel.Attribute.TEX
            else -> throw KorenderException("Unknown attibute $name")
        }


    private fun Mesh.attrBytes(): Map<Attribute, ByteArray> =
        attributes.associate { attr ->
            val nbb = NativeFloatBuffer(vertices.size * attr.structSize)
            vertices.forEach {
                when(attr.name) {
                    "tex" -> {
                        val v = it[attr] as Vec2
                        nbb.put(v.x)
                        nbb.put(v.y)
                    }
                    "pos", "normal" -> {
                        val v = it[attr] as Vec3
                        nbb.put(v.x)
                        nbb.put(v.y)
                        nbb.put(v.z)
                    }
                }
            }
            attr.toKrAttribute() to bytes(nbb)
        }

    private fun Mesh.indexBytes() = indices?.let {
        val nbb = NativeByteBuffer(indices!!.size * 4)
        indices!!.forEach { nbb.put(it) }
        bytes(nbb)
    }

}




