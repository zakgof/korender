package com.zakgof.korender.impl.engine

import com.zakgof.korender.RetentionPolicy

internal object ImmediatelyFreeRetentionPolicy: RetentionPolicy

internal object KeepForeverRetentionPolicy: RetentionPolicy

internal class UntilGenerationRetentionPolicy(val generation: Int) : RetentionPolicy

internal class TimeRetentionPolicy(val seconds: Float) : RetentionPolicy

internal interface Retentionable {
    val retentionPolicy: RetentionPolicy
}