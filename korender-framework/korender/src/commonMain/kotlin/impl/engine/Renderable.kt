package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.geometry.Instanceable
import com.zakgof.korender.impl.geometry.InternalMeshDeclaration
import com.zakgof.korender.impl.glgpu.GlGpuMesh
import com.zakgof.korender.impl.glgpu.GlGpuShader
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.materialDeclaration
import com.zakgof.korender.math.Transform

// TODO Remove this
internal class Renderable(
    val mesh: GlGpuMesh,
    val shader: GlGpuShader,
    val uniforms: Map<String, Any?>,
    val transform: Transform = Transform.IDENTITY
) {
    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        try {
            shader.render(
                { fixer(uniforms[it] ?: contextUniforms[it] ?: if (it == "model") transform.mat4 else null) },
                mesh
            )
        } catch (sr: SkipRender) {
            println("Renderable skipped as resource not ready: [${sr.text}]")
        }
    }
}

internal object Rendering {

    fun render(
        inventory: Inventory,
        declaration: RenderableDeclaration,
        camera: Camera?,
        deferredShading: Boolean,
        contextUniforms: Map<String, Any?>,
        fixer: (Any?) -> Any?,
        defs: Set<String>,
        reverseZ: Boolean = false
    ) {
        val addUniforms = mutableMapOf<String, Any?>()
        val addDefs = mutableSetOf<String>()

        val meshLink = inventory.mesh(declaration.mesh as InternalMeshDeclaration) ?: return
        if (declaration.mesh is Instanceable) {
            declaration.mesh.instancing(meshLink, reverseZ, camera, inventory, addUniforms, addDefs)
        }

        val materialDeclaration = materialDeclaration(declaration.base, deferredShading, declaration.retentionPolicy, declaration.materialModifiers + InternalMaterialModifier { it.shaderDefs += defs + addDefs })
        val shader = inventory.shader(materialDeclaration.shader) ?: return
        // TODO move this to where it is supported
        addUniforms["model"] = declaration.transform.mat4
        shader.render(
            { fixer(materialDeclaration.uniforms[it] ?: contextUniforms[it] ?: addUniforms[it]) },
            meshLink.gpuMesh
        )
    }
}
