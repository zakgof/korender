package com.zakgof.korender.context

import com.zakgof.korender.ShadowAlgorithmDeclaration

interface ShadowContext {
    /**
     * Defines a shadow mapping cascade.
     *
     * @param mapSize shadow texture size
     * @param near distance from camera to this cascade's near plane
     * @param near distance from camera to this cascade's far plane
     * @param fixedYRange optional Y range for occluders for optimization
     * @param algorithm shadow mapping algorithm declaration
     */
    fun Cascade(mapSize: Int, near: Float, far: Float, fixedYRange: Pair<Float, Float>? = null, algorithm: ShadowAlgorithmDeclaration)
}