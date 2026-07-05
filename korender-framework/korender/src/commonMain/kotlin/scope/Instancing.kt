package com.zakgof.korender.scope

import com.zakgof.korender.AnimationDeclaration
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

/**
 * Declaration representing a batch of instanced static meshes.
 */
interface InstancingDeclaration

/**
 * Declaration representing a batch of instanced billboards.
 */
interface BillboardInstancingDeclaration

/**
 * Declaration representing a batch of instanced GLTF models.
 */
interface ModelInstancingDeclaration

/**
 * Scope for declaring individual static mesh instances within a batch.
 */
interface InstancingScope {
    /**
     * Declares a single mesh instance in the batch.
     *
     * @param transform spatial transform of the instance
     * @param color base color modifier for the instance
     * @param metallic metallic factor modifier for the instance
     * @param roughness roughness factor modifier for the instance
     * @param colorTextureIndex index of the color texture in the texture array
     */
    fun Instance(
        transform: Transform? = null,
        color: ColorRGBA? = null,
        metallic: Float? = null,
        roughness: Float? = null,
        colorTextureIndex: Int? = null,
    )
}

/**
 * Scope for declaring individual billboard instances within a batch.
 */
interface BillboardInstancingScope {
    /**
     * Declares a single billboard instance in the batch.
     *
     * @param pos center position of the billboard
     * @param scale scaling factors for the billboard width and height
     * @param rotation rotation angle in radians
     * @param color base color modifier for the billboard instance
     * @param colorTextureIndex index of the texture in the texture array
     */
    fun Instance(
        pos: Vec3? = null,
        scale: Vec2? = null,
        rotation: Float? = null,
        color: ColorRGBA? = null,
        colorTextureIndex: Int? = null,
    )
}

/**
 * Scope for declaring individual model instances within a batch.
 */
interface ModelInstancingScope {
    /**
     * Declares a single model instance in the batch.
     *
     * @param transform spatial transform of the model instance
     * @param time time offset for animations in seconds
     * @param animation animation declaration to override the animation for this instance
     */
    fun Instance(
        transform: Transform,
        time: Float? = null,
        animation: AnimationDeclaration? = null,
    )
}

/**
 * Parameter defining which attributes are enabled for static mesh instancing.
 */
interface InstancingParameter

/**
 * Parameter defining which attributes are enabled for billboard instancing.
 */
interface BillboardInstancingParameter