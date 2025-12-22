package com.zakgof.korender.baker.editor.util

import com.zakgof.korender.impl.buffer.NativeFloatBuffer
import com.zakgof.korender.impl.scene.SceneModel
import com.zakgof.korender.impl.scene.SceneModel.Attribute
import com.zakgof.korender.math.Mat4
import editor.model.Model
import editor.model.brush.BrushMesh
import java.io.File

object ModelCompiler {
    fun compile(model: Model): SceneModel {
        // TODO
        return SceneModel(
            textures = model.materials.values
                .filter { it.colorTexture != null }
                .map {
                    val file = File(it.colorTexture!!.path)
                    val image = file.readBytes()
                    SceneModel.Texture(it.id, file.extension, image)
                }.associateBy { it.id },
            materials = model.materials.values.map { material ->
                SceneModel.Material(
                    material.id,
                    material.baseColor,
                    material.colorTexture?.let { material.id }
                )
            }.associateBy { it.id },
            meshes = model.brushes.values.map { brush ->
                val mesh = brush.mesh
                SceneModel.Mesh(
                    brush.id,
                    mesh.faces.values.flatten().flatMap { it.points }.size,
                    0,
                    mapOf(
                        Attribute.POS to posBytes(mesh),
                        Attribute.NORMAL to normalBytes(mesh),
                        Attribute.TEX to texBytes(mesh)
                    ),
                    null
                )
            }.associateBy { it.id },
            renderables = model.brushes.values.map { brush ->
                SceneModel.Renderable(
                    brush.id,
                    brush.id,
                    brush.faces[0].materialId,
                    Mat4.IDENTITY.asArray()
                )
            }.associateBy { it.id }
        )
    }

    private fun posBytes(mesh: BrushMesh): ByteArray {
        val poses = mesh.faces.values.flatten().flatMap { it.points }
        val nbb = NativeFloatBuffer(poses.size * 3)
        poses.forEach {
            nbb.put(it.x)
            nbb.put(it.y)
            nbb.put(it.z)
        }
        return bytes(nbb)
    }

    private fun normalBytes(mesh: BrushMesh): ByteArray {
        val normals = mesh.faces.values.flatten().map { it.normal }
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

    private fun texBytes(mesh: BrushMesh): ByteArray {
        val texes = mesh.faces.values.flatten().flatMap { it.tex }
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
}


