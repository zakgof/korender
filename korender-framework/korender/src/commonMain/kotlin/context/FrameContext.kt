package com.zakgof.korender.context

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

interface FrameContext {

    val frameInfo: FrameInfo

    fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform = Transform(), transparent: Boolean = false)
    fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3 = Vec3.ZERO, transparent: Boolean = false)
    fun Screen(vararg materialModifiers: MaterialModifier)
    fun Sky(vararg materialModifiers: MaterialModifier)
    fun Gui(block: GuiContainerContext.() -> Unit)
    fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean = false, transparent: Boolean = false, block: InstancedRenderablesContext.() -> Unit)
    fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean = false, block: InstancedBillboardsContext.() -> Unit)
    fun Scene(gltfResource: String, transform: Transform = Transform())

    fun Filter(vararg materialModifiers: MaterialModifier)

    fun DirectionalLight(direction: Vec3, color: Color = Color.White, block: ShadowContext.() -> Unit = {})
    fun PointLight(position: Vec3, color: Color = Color.White)
    fun AmbientLight(color: Color)
}