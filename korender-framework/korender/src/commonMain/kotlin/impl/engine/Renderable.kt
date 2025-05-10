package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.context.DefaultInstancedBillboardsContext
import com.zakgof.korender.impl.context.DefaultInstancedRenderablesContext
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.Mesh
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Transform

internal class Renderable(
    val mesh: Mesh,
    val shader: GlGpuShader,
    val uniforms: Map<String, Any?>,
    val transform: Transform = Transform()
) {
    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        shader.render(
            { fixer(uniforms[it] ?: contextUniforms[it] ?: if (it == "model") transform.mat4 else null )},
            mesh.gpuMesh
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
        vararg defs: String
    ) {
        val materialDeclaration = materialDeclaration(declaration.base, deferredShading, *declaration.materialModifiers.toTypedArray(), InternalMaterialModifier { it.shaderDefs += defs })

        val mesh = inventory.mesh(declaration.mesh) ?: return
        val shader = inventory.shader(materialDeclaration.shader) ?: return

        val meshDeclaration = declaration.mesh.meshDeclaration

        if (meshDeclaration is CustomMesh && meshDeclaration.dynamic) {
            (mesh as Geometry.DefaultMesh).updateMesh(meshDeclaration.block)
        }
        if (meshDeclaration is InstancedBillboard) {
            // TODO: static
            val instances = mutableListOf<BillboardInstance>();
            DefaultInstancedBillboardsContext(instances).apply(meshDeclaration.block)
            if (meshDeclaration.transparent) {
                instances.sortBy { (camera.mat4 * it.pos).z }
            }
            (mesh as Geometry.MultiMesh).updateBillboardInstances(instances)
        }
        if (meshDeclaration is InstancedMesh) {
            mesh as Geometry.MultiMesh
            if (!meshDeclaration.static || !mesh.isInitialized()) {
                val instances = mutableListOf<MeshInstance>()
                DefaultInstancedRenderablesContext(instances).apply(meshDeclaration.block)
                if (meshDeclaration.transparent) {
                    instances.sortBy { (camera.mat4 * it.transform.offset()).z }
                }
                mesh.updateInstances(instances)
            }
        }
        shader.render(
            { fixer(materialDeclaration.uniforms[it] ?: contextUniforms[it] ?: if (it == "model") declaration.transform.mat4 else null) },
            mesh.gpuMesh
        )
    }
}
