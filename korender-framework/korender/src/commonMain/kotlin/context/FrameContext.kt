package com.zakgof.korender.context

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.Prefab
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

interface FrameContext : KorenderContext {

    val frameInfo: FrameInfo

    fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform = Transform(), transparent: Boolean = false)
    fun Renderable(vararg materialModifiers: MaterialModifier, prefab: Prefab)

    fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3 = Vec3.ZERO, transparent: Boolean = false)
    fun Screen(vararg materialModifiers: MaterialModifier)
    fun Sky(vararg materialModifiers: MaterialModifier)
    fun Gui(block: GuiContainerContext.() -> Unit)
    fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: Any, count: Int, mesh: MeshDeclaration, static: Boolean = false, transparent: Boolean = false, block: InstancedRenderablesContext.() -> Unit)
    fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: Any, count: Int, transparent: Boolean = false, block: InstancedBillboardsContext.() -> Unit)
    fun Gltf(resource: String, animation:Int = 0, transform: Transform = Transform(), time: Float? = null)

    fun PostProcess(vararg materialModifiers: MaterialModifier)

    fun DirectionalLight(direction: Vec3, color: ColorRGB = ColorRGB.White, block: ShadowContext.() -> Unit = {})
    fun PointLight(position: Vec3, color: ColorRGB = ColorRGB.White, attenuationLinear: Float = 0.1f, attenuationQuadratic: Float = 0.01f)
    fun AmbientLight(color: ColorRGB)

    fun DeferredShading(block: DeferredShadingContext.() -> Unit = {})

    fun CaptureEnv(slot: Int, resolution: Int, position: Vec3 = Vec3.ZERO, near: Float = 10f, far: Float = 1000f, block: FrameContext.() -> Unit)
}