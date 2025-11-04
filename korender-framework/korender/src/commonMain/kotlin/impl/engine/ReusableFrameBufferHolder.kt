package com.zakgof.korender.impl.engine

import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.impl.glgpu.GlGpuTexture

internal class ReusableFrameBufferHolder {

    private val locks = mutableMapOf<String, String>()

    fun request(target: FrameTarget, currentRetentionPolicy: RetentionPolicy): FrameBufferDeclaration {
        var index = 0
        while(true) {
            val fbName = "reusable-${target.width}x${target.height}-$index"
            if (!locks.values.contains(fbName)) {
                locks[target.colorOutput] = fbName
                locks[target.depthOutput] = fbName
                return FrameBufferDeclaration(fbName, target.width, target.height, listOf(GlGpuTexture.Preset.RGBFilter), true, TransientProperty(currentRetentionPolicy))
            }
            index++
        }
    }

    fun unlock(textureName: String) {
        locks.remove(textureName)
    }
}