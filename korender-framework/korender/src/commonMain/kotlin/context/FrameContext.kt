package com.zakgof.korender.context

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

interface FrameContext : KorenderContext {

    /**
     * Frame information.
     */
    val frameInfo: FrameInfo

    /**
     * Renders a mesh.
     *
     * @param materialModifiers object surface material modifiers
     * @param mesh geometry mesh declaration
     * @param transform model space transformation
     * @param transparent true if the object has transparency
     * @param instancing instancing declaration to render multiple objects in a batch
     */
    fun Renderable(vararg materialModifiers: MaterialModifier, mesh: MeshDeclaration, transform: Transform = Transform.IDENTITY, transparent: Boolean = false, instancing: InstancingDeclaration? = null)

    /**
     * Renders a geometry prefab.
     *
     * @param materialModifiers object surface material modifiers
     * @param prefab geometry prefab
     */
    fun Renderable(vararg materialModifiers: MaterialModifier, prefab: Prefab)

    /**
     * Renders a GLTF model from a resource file.
     *
     * @param resource resource file
     * @param transform model space transform
     * @param time overridden time for animation
     * @param animation index of animation to apply
     * @param instancing instancing declaration to render multiple objects in a batch
     */
    fun Gltf(resource: String, transform: Transform = Transform.IDENTITY, time: Float? = null, animation:Int? = null, instancing: GltfInstancingDeclaration? = null)

    fun Gltf(id: String, bytes: ByteArray, transform: Transform = Transform.IDENTITY, time: Float? = null, animation:Int? = null, instancing: GltfInstancingDeclaration? = null)

    /**
     * Renders a billboard - camera facing quad.
     *
     * @param materialModifiers object surface material modifiers
     * @param transparent true if object has transparency
     * @param instancing instancing declaration to render multiple objects in a batch
     */
    fun Billboard(vararg materialModifiers: MaterialModifier, transparent: Boolean = false, instancing: BillboardInstancingDeclaration? = null)

    /**
     * Renders a sky.
     *
     * @param materialModifiers material modifiers
     */
    fun Sky(vararg materialModifiers: MaterialModifier)

    /**
     * Renders GUI overlay.
     *
     * @param block GUI declaration
     */
    fun Gui(block: GuiContainerContext.() -> Unit)

    /**
     * Adds a post processing effect to a frame.
     *
     * @param postProcessingEffect post processing effect
     * @param block geometry to be rendered after this effect
     */
    fun PostProcess(postProcessingEffect: PostProcessingEffect, block: FrameContext.() -> Unit = {})

    /**
     * Adds a post processing effect with the given material modifiers.
     *
     * @param materialModifiers post processing effect material modifiers
     * @param block geometry to be rendered after this effect
     */
    fun PostProcess(vararg materialModifiers: MaterialModifier, block: FrameContext.() -> Unit = {})

    /**
     * Adds a directional light to the frame.
     *
     * @param direction light direct (may be not normalized)
     * @param color light color
     * @param block shadow cascades declaration block
     */
    fun DirectionalLight(direction: Vec3, color: ColorRGB = ColorRGB.White, block: ShadowContext.() -> Unit = {})

    /**
     * Adds a point light to the frame.
     *
     * @param position light source positions
     * @param color light color
     * @param attenuationLinear linear attenuation factor
     * @param attenuationQuadratic quadratic attenuation factor
     */
    fun PointLight(position: Vec3, color: ColorRGB = ColorRGB.White, attenuationLinear: Float = 0.1f, attenuationQuadratic: Float = 0.01f)

    /**
     * Configures ambient light in the frame.
     *
     * @param color light color
     */
    fun AmbientLight(color: ColorRGB)

    /**
     * Enables deferred shading pipeline.
     *
     * @param block deferred shading pipeline configuration block
     */
    fun DeferredShading(block: DeferredShadingContext.() -> Unit = {})

    /**
     * Renders a scene into an environment probe.
     *
     * @param envProbeName output env probe name
     * @param resolution probe cube texture resolution
     * @param position camera position to capture scene
     * @param near capture camera's near clipping plane
     * @param far capture camera's far clipping plane (effectively, scene bounding radius)
     * @param insideOut experimental option to enable radiant capture
     * @param block scene to capture
     */
    fun CaptureEnv(envProbeName: String, resolution: Int, position: Vec3 = Vec3.ZERO, near: Float = 10f, far: Float = 1000f, insideOut: Boolean = false, block: FrameContext.() -> Unit)

    /**
     * Renders a scene into an frame probe.
     *
     * @param frameProbeName output frame probe name
     * @param width probe texture width
     * @param height probe texture height
     * @param camera capture camera declaration
     * @param projection capture projection declaration
     * @param block scene to capture
     */
    fun CaptureFrame(frameProbeName: String, width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameContext.() -> Unit)

    /**
     * Sets a loader scene to display while the resources are being loaded.
     *
     * @param block loader scene
     */
    fun OnLoading(block: FrameContext.() -> Unit)
}
