package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.ShadowAlgorithmDeclaration

internal class InternalVsmParams(val blurRadius: Float?) : ShadowAlgorithmDeclaration

internal class InternalHardParams() : ShadowAlgorithmDeclaration

internal class InternalPccfParams(val samples: Int, val blurRadius: Float) : ShadowAlgorithmDeclaration
