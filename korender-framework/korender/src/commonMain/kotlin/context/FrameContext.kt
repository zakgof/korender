package com.zakgof.korender.context

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

interface FrameContext : KorenderContext {

    val frameInfo: FrameInfo

    fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform = Transform(), transparent: Boolean = false, instancing: InstancingDeclaration? = null)
    fun Renderable(vararg materialModifiers: MaterialModifier, prefab: Prefab)

    fun Billboard(vararg materialModifiers: MaterialModifier, position: Vec3 = Vec3.ZERO, transparent: Boolean = false)
    fun Sky(vararg materialModifiers: MaterialModifier)
    fun Gui(block: GuiContainerContext.() -> Unit)
    fun InstancedRenderables(vararg materialModifiers: MaterialModifier, id: String, count: Int, mesh: MeshDeclaration, static: Boolean = false, transparent: Boolean = false, block: InstancedRenderablesContext.() -> Unit)
    fun InstancedBillboards(vararg materialModifiers: MaterialModifier, id: String, count: Int, static: Boolean = false, transparent: Boolean = false, block: InstancedBillboardsContext.() -> Unit)
    fun Gltf(resource: String, animation:Int = 0, transform: Transform = Transform(), time: Float? = null)

    fun PostProcess(vararg materialModifiers: MaterialModifier, block: FrameContext.() -> Unit = {})

    fun DirectionalLight(direction: Vec3, color: ColorRGB = ColorRGB.White, block: ShadowContext.() -> Unit = {})
    fun PointLight(position: Vec3, color: ColorRGB = ColorRGB.White, attenuationLinear: Float = 0.1f, attenuationQuadratic: Float = 0.01f)
    fun AmbientLight(color: ColorRGB)

    fun DeferredShading(block: DeferredShadingContext.() -> Unit = {})

    fun CaptureEnv(envProbeName: String, resolution: Int, position: Vec3 = Vec3.ZERO, near: Float = 10f, far: Float = 1000f, insideOut: Boolean = false, defs: Set<String> = setOf(), block: FrameContext.() -> Unit)
    fun CaptureFrame(frameProbeName: String, width: Int, height: Int, cameraDeclaration: CameraDeclaration, projectionDeclaration: ProjectionDeclaration, block: FrameContext.() -> Unit)
}