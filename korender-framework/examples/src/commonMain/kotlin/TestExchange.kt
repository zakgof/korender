package com.zakgof.korender.examples

import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Image
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object TestExchange {

    val fi = AtomicReference<FrameInfo?>(null)
    val screenshotStore = AtomicReference<Image?>(null)

    fun report(frameInfo: FrameInfo?) {
        fi.store(frameInfo)
    }

    fun screenshot(screenshot: Image?) {
        screenshotStore.store(screenshot)
    }
}
