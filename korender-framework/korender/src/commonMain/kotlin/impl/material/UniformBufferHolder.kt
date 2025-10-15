package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.gl.GL.glGetInteger
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_UNIFORM_BLOCK_SIZE
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_UNIFORM_BUFFER_BINDINGS
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
import com.zakgof.korender.impl.glgpu.GlGpuUniformBuffer
import com.zakgof.korender.impl.glgpu.UniformBlock

internal class UniformBufferHolder {

    private val frameUbo = GlGpuUniformBuffer(4650)

    private val frameOffsets = mapOf(
        "cameraPos" to 0,
        "cameraDir" to 16,
        "view" to 32,
        "projectionWidth" to 96,
        "projectionHeight" to 100,
        "projectionNear" to 104,
        "projectionFar" to 108,
        "screenWidth" to 112,
        "screenHeight" to 116,
        "time" to 120,
        "ambientColor" to 128,
        "numDirectionalLights" to 140,
        "directionalLightDir[0]" to 144,
        "directionalLightColor[0]" to 656,
        "directionalLightShadowTextureIndex[0]" to 1168,
        "directionalLightShadowTextureCount[0]" to 1680,
        "numPointLights" to 2192,
        "pointLightPos[0]" to 2208,
        "pointLightColor[0]" to 2720,
        "pointLightAttenuation[0]" to 3232,
        "numShadows" to 3744,
        "bsps[0]" to 3760,
        "cascade[0]" to 4080,
        "yMin[0]" to 4160,
        "yMax[0]" to 4240,
        "shadowMode[0]" to 4320,
        "f1[0]" to 4400,
        "f2[0]" to 4480,
        "i1[0]" to 4560
    )

    private val bufferOffsetAlignment = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT)
    private val maxBindings = glGetInteger(GL_MAX_UNIFORM_BUFFER_BINDINGS)
    private val shaderUboSize = glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE)

    private val shaderUbo = GlGpuUniformBuffer(shaderUboSize)

    private val renderQueue = mutableListOf<RenderItem>()
    private var bufferShift = 0
    private var currentBinding = 1

    init {
        frameUbo.bindBase(0)
    }

    fun populateFrame(uniforms: (String) -> Any?, ignoreMissing: Boolean = false) {
        frameUbo.populate(uniforms, 0, frameOffsets, "FrameContext", ignoreMissing)
        frameUbo.upload(4608)
    }

    fun populate(
        uniforms: (String) -> Any?,
        uniformBlock: UniformBlock?,
        materialName: String,
        render: (Int) -> Boolean
    ): Int? {
        val renderItem = if (uniformBlock != null) {
            if (shaderUboSize - bufferShift < uniformBlock.size || currentBinding >= maxBindings) {
                flush()
            }
            shaderUbo.populate(uniforms, bufferShift, uniformBlock.offsets, materialName)
            val ri = RenderItem(render, bufferShift, uniformBlock.size, currentBinding)
            bufferShift = ((bufferShift + uniformBlock.size + bufferOffsetAlignment - 1) / bufferOffsetAlignment) * bufferOffsetAlignment
            currentBinding++
            ri
        } else {
            RenderItem(render, bufferShift, 0, null)
        }
        renderQueue += renderItem
        return renderItem.binding
    }

    fun flush(): Boolean {
        var success = true
        if (renderQueue.isNotEmpty()) {
            shaderUbo.upload(bufferShift)
            renderQueue.forEach { renderItem ->
                renderItem.binding?.let { shaderUbo.bindRange(it, renderItem.shift, renderItem.size) }
                success = success and renderItem.render(renderItem.binding ?: -1)
            }
        }
        bufferShift = 0
        currentBinding = 1
        renderQueue.clear()
        return success
    }


    private class RenderItem(
        val render: (Int) -> Boolean,
        val shift: Int,
        val size: Int,
        val binding: Int?
    )
}