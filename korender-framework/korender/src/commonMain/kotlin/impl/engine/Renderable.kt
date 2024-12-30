package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.InstancedBillboard
import com.zakgof.korender.impl.geometry.InstancedMesh
import com.zakgof.korender.impl.geometry.Mesh
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.material.DynamicUniforms
import com.zakgof.korender.math.Transform

internal class Renderable(val mesh: Mesh, val shader: GlGpuShader, val uniforms: DynamicUniforms, val transform: Transform = Transform()) {

    companion object {
        fun create(inventory: Inventory, declaration: RenderableDeclaration, camera: Camera, isShadowCaster: Boolean, shadowCascades: Int = 0): Renderable? {
            val mesh = inventory.mesh(declaration.mesh) ?: return null

            val additionalShadowFlags = if (isShadowCaster) listOf("SHADOW_CASTER") else (0..<shadowCascades).map { "SHADOW_RECEIVER$it" }
            val origShader = declaration.shader
            val modifiedShader = ShaderDeclaration(
                origShader.vertFile, origShader.fragFile, origShader.defs + additionalShadowFlags,
                origShader.plugins
            )
            val shader = inventory.shader(modifiedShader) ?: return null

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
            val uniforms = declaration.uniforms
            val transform = declaration.transform
            return Renderable(mesh, shader, uniforms, transform)
        }
    }

    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?, shaderOverride: GlGpuShader = this.shader) {
        val totalUniformSupplier = uniforms.invoke() + contextUniforms + mapOf("model" to transform.mat4)
        shaderOverride.render(
            { fixer(totalUniformSupplier[it]) },
            mesh.gpuMesh
        )
    }

}
