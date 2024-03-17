package com.zakgof.korender

import com.zakgof.korender.declaration.FilterDeclaration
import com.zakgof.korender.declaration.InstancedBillboardsContext
import com.zakgof.korender.declaration.InstancedRenderablesContext
import com.zakgof.korender.declaration.MaterialDeclaration
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.RenderableDeclaration
import com.zakgof.korender.declaration.ShaderDeclaration
import com.zakgof.korender.material.StockUniforms
import com.zakgof.korender.material.UniformSupplier
import com.zakgof.korender.math.Transform

class SceneContext(val frameInfo: FrameInfo, private val sceneBuilder: SceneDeclaration) {
    fun Renderable(
        mesh: MeshDeclaration,
        material: MaterialDeclaration,
        transform: Transform = Transform()
    ) = sceneBuilder.add(
        RenderableDeclaration(mesh, material.shader, material.uniforms, transform)
    )

    fun Billboard(fragment: String = "billboard.frag", material: StockUniforms.() -> Unit) =
        sceneBuilder.add(
            RenderableDeclaration(
                MeshDeclaration.BillboardDeclaration,
                ShaderDeclaration("billboard.vert", fragment, setOf()),
                StockUniforms().apply(material)
            )
        ) // TODO Bucket

    fun Filter(fragment: String, uniforms: UniformSupplier = UniformSupplier { null }) =
        sceneBuilder.add(FilterDeclaration(fragment, uniforms))

    fun InstancedBillboards(
        id: Any,
        count: Int,
        zSort: Boolean = false,
        fragment: String = "standard.frag",
        material: StockUniforms.() -> Unit,
        block: InstancedBillboardsContext.() -> Unit
    ) =
        sceneBuilder.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedBillboardDeclaration(id, count, zSort, block),
                ShaderDeclaration("billboard.vert", fragment, setOf()),
                StockUniforms().apply(material)
            )
        ) // TODO Bucket

    fun InstancedRenderables(
        id: Any,
        count: Int,
        mesh: MeshDeclaration,
        material: MaterialDeclaration,
        block: InstancedRenderablesContext.() -> Unit
    ) =
        sceneBuilder.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedRenderableDeclaration(id, count, mesh, material, block),
                material.shader,
                material.uniforms
            )
        ) // TODO Bucket
}