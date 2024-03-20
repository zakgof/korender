package com.zakgof.korender

import com.zakgof.korender.declaration.ContainerContext
import com.zakgof.korender.declaration.Direction
import com.zakgof.korender.declaration.ElementDeclaration
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

class SceneContext(val frameInfo: FrameInfo, private val sceneDeclaration: SceneDeclaration) {
    fun Renderable(
        mesh: MeshDeclaration,
        material: MaterialDeclaration,
        transform: Transform = Transform()
    ) = sceneDeclaration.add(
        RenderableDeclaration(mesh, material.shader, material.uniforms, transform)
    )

    fun Billboard(fragment: String = "billboard.frag", material: StockUniforms.() -> Unit) =
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.BillboardDeclaration,
                ShaderDeclaration("billboard.vert", fragment, setOf()),
                StockUniforms().apply(material)
            )
        ) // TODO Bucket

    fun Filter(fragment: String, uniforms: UniformSupplier = UniformSupplier { null }) =
        sceneDeclaration.add(FilterDeclaration(fragment, uniforms))

    fun InstancedBillboards(
        id: Any,
        count: Int,
        zSort: Boolean = false,
        fragment: String = "standard.frag",
        material: StockUniforms.() -> Unit,
        block: InstancedBillboardsContext.() -> Unit
    ) =
        sceneDeclaration.add(
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
        static: Boolean = false,
        block: InstancedRenderablesContext.() -> Unit,
    ) =
        sceneDeclaration.add(
            RenderableDeclaration(
                MeshDeclaration.InstancedRenderableDeclaration(id, count, mesh, material, static, block),
                material.shader,
                material.uniforms
            )
        ) // TODO Bucket

    fun Gui(block: ContainerContext.() -> Unit) {
        val gui = ElementDeclaration.ContainerDeclaration(Direction.Vertical)
        sceneDeclaration.gui = gui
        ContainerContext(gui).apply(block)
    }

}