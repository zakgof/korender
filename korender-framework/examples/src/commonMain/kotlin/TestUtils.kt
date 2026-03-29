package com.zakgof.korender.examples

import com.zakgof.korender.FrameInfo
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object TestUtils {

    val fi = AtomicReference<FrameInfo?>(null)

    fun report(frameInfo: FrameInfo?) {
        fi.store(frameInfo)
    }
}
