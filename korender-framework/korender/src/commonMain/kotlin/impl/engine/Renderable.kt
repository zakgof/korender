package com.zakgof.korender.impl.engine

import com.zakgof.korender.Attributes.INSTPOS
import com.zakgof.korender.Attributes.INSTROT
import com.zakgof.korender.Attributes.INSTSCALE
import com.zakgof.korender.Attributes.MODEL0
import com.zakgof.korender.Attributes.MODEL1
import com.zakgof.korender.Attributes.MODEL2
import com.zakgof.korender.Attributes.MODEL3
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Transform

internal class Renderable(
    val mesh: GlGpuMesh,
    val shader: GlGpuShader,
    val uniforms: Map<String, Any?>,
    val transform: Transform = Transform.IDENTITY
) {
    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        shader.render(
            { fixer(uniforms[it] ?: contextUniforms[it] ?: if (it == "model") transform.mat4 else null) },
            mesh
        )
    }
}

internal object Rendering {

    fun render(
        inventory: Inventory,
        declaration: RenderableDeclaration,
        camera: Camera,
        deferredShading: Boolean,
        contextUniforms: Map<String, Any?>,
        fixer: (Any?) -> Any?,
        defs: Set<String>,
        reverseZ: Boolean = false
    ) {
        val addDefs = if (declaration.mesh is InstancedMesh || declaration.mesh is InstancedBillboard) setOf("INSTANCING") else setOf()
        val materialDeclaration = materialDeclaration(declaration.base, deferredShading, declaration.retentionPolicy, declaration.materialModifiers + InternalMaterialModifier { it.shaderDefs += defs + addDefs })

        val meshLink = inventory.mesh(declaration.mesh as InternalMeshDeclaration) ?: return
        val shader = inventory.shader(materialDeclaration.shader) ?: return

        if (declaration.mesh is CustomMesh && declaration.mesh.dynamic) {
            meshLink.cpuMesh.updateMesh(declaration.mesh.block)
            meshLink.updateGpu(0)
        }
        if (declaration.mesh is InstancedBillboard) {
            val mesh = meshLink.cpuMesh
            if (!declaration.mesh.static || !mesh.instancesInitialized || declaration.mesh.transparent) {
                var instances = declaration.mesh.instancer()
                val sortFactor = if (reverseZ) -1f else 1f
                if (declaration.mesh.transparent) {
                    instances = instances.sortedBy { (camera.mat4 * it.pos).z * sortFactor }
                }
                mesh.updateMesh {
                    instances.forEachIndexed { i, it ->
                        this.attrSet(INSTPOS, i, it.pos)
                        this.attrSet(INSTROT, i, it.phi)
                        this.attrSet(INSTSCALE, i, it.scale)
                    }
                }
                meshLink.updateGpu(instances.size)
            }
        }
        if (declaration.mesh is InstancedMesh) {
            val mesh = meshLink.cpuMesh
            if (!declaration.mesh.static || !mesh.instancesInitialized || declaration.mesh.transparent) {
                var instances = declaration.mesh.instancer()
                val sortFactor = if (reverseZ) -1f else 1f
                if (declaration.mesh.transparent) {
                    instances = instances.sortedBy { (camera.mat4 * it.transform.offset()).z * sortFactor }
                }
                mesh.updateMesh {
                    instances.forEachIndexed { i, it ->
                        val m = it.transform.mat4
                        this.attrSet(MODEL0, i, floatArrayOf(m.m00, m.m10, m.m20, m.m30))
                        this.attrSet(MODEL1, i, floatArrayOf(m.m01, m.m11, m.m21, m.m31))
                        this.attrSet(MODEL2, i, floatArrayOf(m.m02, m.m12, m.m22, m.m32))
                        this.attrSet(MODEL3, i, floatArrayOf(m.m03, m.m13, m.m23, m.m33))
                    }
                }
                mesh.instancesInitialized = true
                meshLink.updateGpu(instances.size)
            }
        }
        shader.render(
            { fixer(materialDeclaration.uniforms[it] ?: contextUniforms[it] ?: if (it == "model") declaration.transform.mat4 else null) },
            meshLink.gpuMesh
        )
    }
}
