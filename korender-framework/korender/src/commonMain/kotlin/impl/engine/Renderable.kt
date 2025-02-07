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
import com.zakgof.korender.impl.material.DynamicUniforms
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Transform

internal class Renderable(
    val mesh: Mesh,
    val shader: GlGpuShader,
    val uniforms: DynamicUniforms,
    val transform: Transform = Transform()
) {
    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        shader.render(
            { fixer(uniforms.invoke()[it] ?: contextUniforms[it] ?: if (it == "model") transform.mat4 else null )},
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

        if (declaration.mesh is CustomMesh && declaration.mesh.dynamic) {
            (mesh as Geometry.DefaultMesh).updateMesh(declaration.mesh.block)
        }
        if (declaration.mesh is InstancedBillboard) {
            // TODO: static
            val instances = mutableListOf<BillboardInstance>();
            DefaultInstancedBillboardsContext(instances).apply(declaration.mesh.block)
            if (declaration.mesh.transparent) {
                instances.sortBy { (camera.mat4 * it.pos).z }
            }
            (mesh as Geometry.MultiMesh).updateBillboardInstances(instances)
        }
        if (declaration.mesh is InstancedMesh) {
            mesh as Geometry.MultiMesh
            if (!declaration.mesh.static || !mesh.isInitialized()) {
                val instances = mutableListOf<MeshInstance>()
                DefaultInstancedRenderablesContext(instances).apply(declaration.mesh.block)
                if (declaration.mesh.transparent) {
                    instances.sortBy { (camera.mat4 * it.transform.offset()).z }
                }
                mesh.updateInstances(instances)
            }
        }
        shader.render(
            { fixer(materialDeclaration.uniforms.invoke()[it] ?: contextUniforms[it] ?: if (it == "model") declaration.transform.mat4 else null) },
            mesh.gpuMesh
        )
    }
}
