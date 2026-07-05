package com.zakgof.korender.scope

import com.zakgof.korender.AnimationDeclaration
import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.BillboardMaterial
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Material
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.ModelInfo
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.PostProcessingMaterial
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.TerrainMaterialScope
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3

/**
 * Scope for declaring a single frame's renderable elements, post-processing filters, lights, and layout hierarchies.
 */
interface FrameScope : KorenderScope {

    /**
     * Frame information.
     */
    val frameInfo: FrameInfo

    /**
     * Renders a mesh.
     *
     * @param material object surface material
     * @param mesh geometry mesh declaration
     * @param transform model space transformation
     * @param transparent true if the object has transparency
     * @param instancing instancing declaration to render multiple objects in a batch
     */
    fun Renderable(material: Material, mesh: MeshDeclaration, transform: Transform = Transform.IDENTITY, transparent: Boolean = false, instancing: InstancingDeclaration? = null)

    /**
     * Renders a GLTF model from a resource file.
     *
     * @param resource resource file
     * @param transform model space transform
     * @param instancing instancing declaration to render multiple objects in a batch
     * @param animation animation declaration to override the default animation (if present)
     * @param onUpdate callback with runtime model details
     * @param materialModifier material modifiers block
     */

    fun Model(resource: String, transform: Transform = Transform.IDENTITY, instancing: ModelInstancingDeclaration? = null, animation: AnimationDeclaration? = null, onUpdate: ((ModelInfo) -> Unit)? = null, materialModifier: BaseMaterialScope.() -> Unit = {})


    /**
     * Renders a billboard - camera facing quad.
     *
     * @param material object surface material
     * @param transparent true if object has transparency
     * @param instancing instancing declaration to render multiple objects in a batch
     */
    fun Billboard(material: BillboardMaterial, transparent: Boolean = false, instancing: BillboardInstancingDeclaration? = null)

    /**
     * Renders a sky.
     *
     * @param material sky material
     */
    fun Sky(material: SkyMaterial)

    /**
     * Renders GUI overlay.
     *
     * @param block GUI declaration
     */
    fun Gui(block: GuiContainerScope.() -> Unit)

    /**
     * Adds a post processing effect to a frame.
     *
     * @param postProcessingEffect post processing effect
     * @param block geometry to be rendered after this effect
     */
    fun PostProcess(postProcessingEffect: PostProcessingEffect, block: FrameScope.() -> Unit = {})

    /**
     * Adds a post processing effect with the given material modifiers.
     *
     * @param material post processing effect material
     * @param block geometry to be rendered after this effect
     */
    fun PostProcess(material: PostProcessingMaterial, block: FrameScope.() -> Unit = {})

    /**
     * Adds a directional light to the frame.
     *
     * @param direction light direct (may be not normalized)
     * @param color light color
     * @param block shadow cascades declaration block
     */
    fun DirectionalLight(direction: Vec3, color: ColorRGB = ColorRGB.White, block: ShadowScope.() -> Unit = {})

    /**
     * Adds a point light to the frame.
     *
     * @param position light source position
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
    fun DeferredShading(block: DeferredShadingScope.() -> Unit = {})

    /**
     * Renders a scene into an environment probe.
     *
     * @param envProbeName output env probe name
     * @param resolution probe cube texture resolution
     * @param block scene to capture
     */
    fun CaptureEnv(envProbeName: String, resolution: Int, block: FrameScope.() -> Unit)

    /**
     * Renders a scene into a frame probe.
     *
     * @param frameProbeName output frame probe name
     * @param width probe texture width
     * @param height probe texture height
     * @param block scene to capture
     */
    fun CaptureFrame(frameProbeName: String, width: Int, height: Int, block: FrameScope.() -> Unit)

    /**
     * Sets a loader scene to display while the resources are being loaded.
     *
     * @param force force displaying the loading screen
     * @param block loader scene
     */
    fun OnLoading(force: Boolean = false, block: FrameScope.() -> Unit)

    /**
     * Creates a child node for hierarchical scene composition.
     *
     * @param resourceLoader overridden resource loader for this node
     * @param transform model space transform for the node
     * @param retentionPolicy resource retention policy for this node
     * @param time override for real time (frameInfo.time) used by animated shaders and GLTF animations
     * @param block node contents
     */
    fun Node(
        resourceLoader: ResourceLoader? = null,
        transform: Transform = Transform.IDENTITY,
        retentionPolicy: RetentionPolicy? = null,
        time: Float? = null,
        block: FrameScope.() -> Unit
    )

    /**
     * Renders a heightfield using clipmaps technique.
     *
     * @param id unique declaration id
     * @param cellSize terrain cell size (at the highest resolution)
     * @param hg parameter affecting number of cells in a clipmap ring
     * @param rings number of visible rings
     * @param block block of rendering parameters
     */
    fun HeightField(id: String, cellSize: Float, hg: Int, rings: Int, block: TerrainMaterialScope.() -> Unit)
}

