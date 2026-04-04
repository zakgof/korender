package com.zakgof.korender.impl.engine

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.context.NodeContext

internal object ImmediatelyFreeRetentionPolicy: RetentionPolicy

internal object KeepForeverRetentionPolicy: RetentionPolicy

internal class UntilGenerationRetentionPolicy(val generation: Int) : RetentionPolicy

internal class TimeRetentionPolicy(val seconds: Float) : RetentionPolicy

internal interface NodeKeeper {
    val nodeContext: NodeContext
}