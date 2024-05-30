package com.zakgof.korender.declaration

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
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
    fun Billboard(material: BillboardMaterialDeclaration, position: Vec3 = Vec3.ZERO, transparent: Boolean = false)
    fun Filter(fragFile: String, vararg defs: String, plugins: Map<String, String> = mapOf(), uniforms: UniformSupplier = UniformSupplier { null })
    fun Sky(vararg defs: String, plugins: Map<String, String> = mapOf(), uniforms: UniformSupplier = UniformSupplier { null })
    fun Shadow(block: ShadowContext.() -> Unit)
    fun Gui(block: GuiContainerContext.() -> Unit)
    fun InstancedRenderables(id: Any, count: Int, mesh: MeshDeclaration, material: MaterialDeclaration, static: Boolean = false, block: InstancedRenderablesContext.() -> Unit)
    fun InstancedBillboards(id: Any, count: Int, material: BillboardMaterialDeclaration, zSort: Boolean = false, block: InstancedBillboardsContext.() -> Unit)

}