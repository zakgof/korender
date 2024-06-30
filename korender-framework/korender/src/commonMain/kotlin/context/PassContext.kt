package com.zakgof.korender.context

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.material.MaterialModifier
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.projection.Projection

interface PassContext {

    val frameInfo: FrameInfo
    val camera: Camera
    val projection: Projection
    val light: Vec3
    val width: Int
    val height: Int

    fun Camera(camera: Camera)
    fun Projection(projection: Projection)
    fun Light(light: Vec3)

    fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform = Transform(), transparent: Boolean = false)
    fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3 = Vec3.ZERO, transparent: Boolean = false)
    fun Screen(vararg materialModifiers: MaterialModifier)
    fun Sky(vararg materialModifiers: MaterialModifier)
    fun Gui(block: GuiContainerContext.() -> Unit)
    fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean = false, transparent: Boolean = false, block: InstancedRenderablesContext.() -> Unit)
    fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean = false, block: InstancedBillboardsContext.() -> Unit)
}