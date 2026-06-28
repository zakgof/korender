package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.context.NodeContext

internal class CaptureContext(
    val frameContext: FrameContext,
    val sceneDeclaration: SceneDeclaration,
    val nodeContext: NodeContext
)
