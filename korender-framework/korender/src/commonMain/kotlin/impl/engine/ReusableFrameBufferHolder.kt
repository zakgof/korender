package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.glgpu.GlGpuTexture

internal class ReusableFrameBufferHolder {

    private val locks = mutableMapOf<String, String>()

    fun request(frameContext: FrameContext, target: FrameTarget, nodeContext: NodeContext): FrameBufferDeclaration {
        var index = 0
        val w = frameContext.width / target.downSample
        val h = frameContext.height / target.downSample
        while(true) {
            val fbName = "reusable-$w:$h-$index"
            if (!locks.values.contains(fbName)) {
                locks[target.colorOutput] = fbName
                locks[target.depthOutput] = fbName
                return FrameBufferDeclaration(fbName, w, h, listOf(GlGpuTexture.Preset.RGBAMipmap), true, TransientProperty(nodeContext))
            }
            index++
        }
    }

    fun unlock(textureName: String) {
        locks.remove(textureName)
    }
}