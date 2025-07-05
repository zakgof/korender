package com.zakgof.korender.impl.engine.shadow

import com.zakgof.korender.ShadowAlgorithmDeclaration

internal class InternalVsmShadow(val blurRadius: Float?) : ShadowAlgorithmDeclaration

internal class InternalHardShadow : ShadowAlgorithmDeclaration

internal class InternalSoftwarePcfShadow(val samples: Int, val blurRadius: Float) : ShadowAlgorithmDeclaration

internal class InternalHardwarePcfShadow(val bias: Float) : ShadowAlgorithmDeclaration

