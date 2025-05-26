package impl.engine

import com.zakgof.korender.RetentionPolicy

internal object ImmediatelyFreeRetentionPolicy: RetentionPolicy

internal object KeepForeverRetentionPolicy: RetentionPolicy

internal class UntilGenerationRetentionPolicy(val generation: Int) : RetentionPolicy

internal class TimeRetentionPolicy(val seconds: Float) : RetentionPolicy

internal class RetentionDecorated<T> (val key: T, val retentionPolicy: RetentionPolicy)

internal interface Retentionable {
    val retentionPolicy: RetentionPolicy
}