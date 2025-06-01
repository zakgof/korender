package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.context.DefaultInstancedBillboardsContext
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.geometry.MultiMesh
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Transform

internal class Renderable(
    val mesh: GlGpuMesh,
    val shader: GlGpuShader,
    val uniforms: Map<String, Any?>,
    val transform: Transform = Transform()
) {
    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        shader.render(
            { fixer(uniforms[it] ?: contextUniforms[it] ?: if (it == "model") transform.mat4 else null )},
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
        val materialDeclaration = materialDeclaration(declaration.base, deferredShading, declaration.retentionPolicy, declaration.materialModifiers + InternalMaterialModifier { it.shaderDefs += defs })

        val meshLink = inventory.mesh(declaration.mesh as InternalMeshDeclaration) ?: return
        val shader = inventory.shader(materialDeclaration.shader) ?: return

        if (declaration.mesh is CustomMesh && declaration.mesh.dynamic) {
            meshLink.cpuMesh.updateMesh(declaration.mesh.block)
            meshLink.updateGpu()
        }
        if (declaration.mesh is InstancedBillboard) {
            val mesh = meshLink.cpuMesh as MultiMesh
            if (!declaration.mesh.static || !mesh.initialized || declaration.mesh.transparent) {
                val instances = mutableListOf<BillboardInstance>();
                DefaultInstancedBillboardsContext(instances).apply(declaration.mesh.block)
                val sortFactor = if (reverseZ) -1f else 1f
                if (declaration.mesh.transparent) {
                    instances.sortBy { (camera.mat4 * it.pos).z * sortFactor }
                }
                meshLink.cpuMesh.updateBillboardInstances(instances)
                meshLink.updateGpu(instances.size * 4, instances.size * 6)
            }
        }
        if (declaration.mesh is InstancedMesh) {
            val mesh = meshLink.cpuMesh as MultiMesh
            if (!declaration.mesh.static || !mesh.initialized || declaration.mesh.transparent) {
                var instances = declaration.mesh.instancer()
                val sortFactor = if (reverseZ) -1f else 1f
                if (declaration.mesh.transparent) {
                    instances = instances.sortedBy { (camera.mat4 * it.transform.offset()).z * sortFactor}
                }
                mesh.updateInstances(instances)
                meshLink.updateGpu(mesh.prototype.vertexCount * instances.size, mesh.prototype.indexCount * instances.size)
            }
        }
        shader.render(
            { fixer(materialDeclaration.uniforms[it] ?: contextUniforms[it] ?: if (it == "model") declaration.transform.mat4 else null) },
            meshLink.gpuMesh
        )
    }
}
