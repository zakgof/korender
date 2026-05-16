package editor.util

import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.NativeFloatBuffer
import com.zakgof.korender.impl.buffer.toByteArray
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.impl.scene.SceneModel.Attribute
import com.zakgof.korender.math.Mat4
import editor.cache.TextureImageCache
import editor.model.Material
import editor.model.Model
import editor.model.brush.BrushMesh
import editor.model.brush.Face
import java.io.File

/**
 *
 */
object ModelCompiler {
    fun compile(model: Model): SceneModel {

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
            it.key to SceneModel.Material(
                id = it.key.toString(),
                baseColor = 0xFFFFFFFF,
                colorTextureId = null,
                colorTextureIds = it.value.map { p -> p.second },
                stochasticSharpness = if (it.key.stochastic) 12f else null,
                triplanarScale = it.key.triplanarScale
            )
        }.toMap()

        val materials = texArrayMaterials.ifEmpty { mapOf("notex" to SceneModel.Material("notex")) }
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
                SceneModel.Mesh(
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

        return SceneModel(
            textures = textures,
            materials = materials.mapKeys { it.key.toString() },
            meshes = meshes.associateBy { it.id },
            renderables = meshes.map {
                SceneModel.Renderable(
                    it.id,
                    it.id,
                    it.id,
                    Mat4.IDENTITY.asArray()
                )
            }.associateBy { it.id }
        )
    }

    private fun texture(path: String): SceneModel.Texture {
        val file = File(path)
        val image = file.readBytes()
        return SceneModel.Texture(file.name, file.extension, image)
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

    private fun bytes(nbb: NativeFloatBuffer): ByteArray {
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
        val triplanarScale: Float?
    )

}




