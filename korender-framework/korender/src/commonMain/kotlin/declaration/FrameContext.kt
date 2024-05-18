package com.zakgof.korender.declaration

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.impl.material.StockUniforms
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

interface FrameContext {

    val frameInfo: FrameInfo
    val camera: Camera
    val projection: Projection
    val light: Vec3
    val width: Int
    val height: Int
    fun Camera(camera: Camera)
    fun Projection(projection: Projection)
    fun Light(light: Vec3)

    fun Renderable(mesh: MeshDeclaration, material: MaterialDeclaration, transform: Transform = Transform(), transparent: Boolean = false)
    fun Billboard(position: Vec3 = Vec3.ZERO, fragment: String = "standard.frag", vararg defs: String, material: StockUniforms.() -> Unit, transparent: Boolean = false)
    fun Filter(fragment: String, uniforms: UniformSupplier = UniformSupplier { null })
    fun Sky(preset: String)
    fun Shadow(block: ShadowContext.() -> Unit)
    fun Gui(block: GuiContainerContext.() -> Unit)
    fun InstancedBillboards(id: Any, count: Int, zSort: Boolean = false, fragment: String = "standard.frag", material: StockUniforms.() -> Unit, block: InstancedBillboardsContext.() -> Unit)
    fun InstancedRenderables(id: Any, count: Int, mesh: MeshDeclaration, material: MaterialDeclaration, static: Boolean = false, block: InstancedRenderablesContext.() -> Unit)

}