package editor.util

import androidx.compose.ui.graphics.toArgb
import com.zakgof.korender.baker.editor.util.toKorender
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.NativeFloatBuffer
import com.zakgof.korender.impl.buffer.toByteArray
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.impl.scene.SceneModel.Attribute
import com.zakgof.korender.math.Mat4
import editor.model.Model
import editor.model.brush.BrushMesh
import editor.model.brush.Face
import java.io.File

object ModelCompiler {
    fun compile(model: Model): SceneModel {

        val textures = model.materials.values
            .filter { it.colorTexture != null }
            .map { it.colorTexture!!.path }
            .distinct()
            .associateWith { texture(it) }

        val texArrayGroups = model.materials.values
            .filter { it.colorTexture != null && it.baseColor.toArgb() == -1 }
            .groupBy({ sizeString(it.colorTexture!!.path) }, { it.id to it.colorTexture!!.path })
            .filter { it.value.size > 1 }

        val matIdGroupies = texArrayGroups
            .flatMap { tag -> tag.value.mapIndexed { index, idToPath -> idToPath.first to (tag.key to index) } }
            .toMap()

        val texArrayMaterials = texArrayGroups.map {
            it.key to SceneModel.Material(
                it.key,
                0xFFFFFFFF,
                null,
                it.value.map { p -> p.second }
            )
        }.toMap()

        val materials = model.materials.values
            .filter { !matIdGroupies.contains(it.id) }
            .map {
                SceneModel.Material(
                    it.id,
                    it.baseColor.toKorender().toLong(),
                    it.colorTexture?.path,
                    null
                )
            } + texArrayMaterials.values

        val meshFaces = model.brushes.values.flatMap { brush -> brush.faces.map { brush.mesh to it } }

        val simpleMaterialFaces = meshFaces
            .filter { !matIdGroupies.containsKey(it.second.materialId) }
            .groupBy { it.second.materialId }
            .map { matToMeshFaces ->
                SceneModel.Mesh(
                    matToMeshFaces.key,
                    matToMeshFaces.value.sumOf { it.first.faces[it.second.plane]!!.size * 3 },
                    0,
                    mapOf(
                        Attribute.POS to posBytes(matToMeshFaces.value),
                        Attribute.NORMAL to normalBytes(matToMeshFaces.value),
                        Attribute.TEX to texBytes(matToMeshFaces.value)
                    ),
                    null
                )
            }

        val arrayMaterialFaces = meshFaces
            .filter { matIdGroupies.containsKey(it.second.materialId) }
            .groupBy({ matIdGroupies[it.second.materialId]!!.first }, { it to matIdGroupies[it.second.materialId]!!.second })
            .map { matToMeshFacesWithId ->
                val faces = matToMeshFacesWithId.value.map { it.first }
                SceneModel.Mesh(
                    matToMeshFacesWithId.key,
                    matToMeshFacesWithId.value.sumOf { it.first.first.faces[it.first.second.plane]!!.size * 3 },
                    0,
                    mapOf(
                        Attribute.POS to posBytes(faces),
                        Attribute.NORMAL to normalBytes(faces),
                        Attribute.TEX to texBytes(faces),
                        Attribute.COLORTEXINDEX to colorTexIndex(matToMeshFacesWithId.value)
                    ),
                    null
                )
            }

        val meshes = simpleMaterialFaces + arrayMaterialFaces

        return SceneModel(
            textures = textures,
            materials = materials.associateBy { it.id },
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
            .flatMap { it.first.faces[it.second.plane]!! }
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
            .flatMap { it.first.faces[it.second.plane]!! }
            .map { it.normal }
        val nbb = NativeFloatBuffer(normals.size * 3 * 3)
        normals.forEach { p ->
            repeat(3) {
                nbb.put(p.x)
                nbb.put(p.y)
                nbb.put(p.z)
            }
        }
        return bytes(nbb)
    }

    private fun colorTexIndex(faces: List<Pair<Pair<BrushMesh, Face>, Int>>): ByteArray {
        val index = faces
            .flatMap { pair -> List(pair.first.first.faces[pair.first.second.plane]!!.size * 3) { pair.second } }
        val nbb = NativeByteBuffer(index.size * 1)
        index.forEach {
            nbb.put(it.toUByte().toByte())
        }
        return nbb.toByteArray()
    }


    private fun texBytes(faces: List<Pair<BrushMesh, Face>>): ByteArray {
        val texes = faces
            .flatMap { it.first.faces[it.second.plane]!! }
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

    private fun sizeString(path: String): String {
        val img = TextureImageCache.compose(path)
        return "${img.width}x${img.height}"
    }

}




