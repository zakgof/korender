package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.gl.GL.glGetInteger
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_UNIFORM_BLOCK_SIZE
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_UNIFORM_BUFFER_BINDINGS
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
import com.zakgof.korender.impl.glgpu.GlGpuUniformBuffer
import com.zakgof.korender.impl.glgpu.UniformBlock

internal class UniformBufferHolder {

    private val frameUbo = GlGpuUniformBuffer(4608)

    private val frameOffsets = mapOf(
        "cameraPos" to 0,
        "cameraDir" to 16,
        "view" to 32,
        "projection" to 96,
        "screenWidth" to 160,
        "screenHeight" to 164,
        "time" to 168,
        "ambientColor" to 176,
        "numDirectionalLights" to 188,
        "directionalLightDir[0]" to 192,
        "directionalLightColor[0]" to 704,
        "directionalLightShadowTextureIndex[0]" to 1216,
        "directionalLightShadowTextureCount[0]" to 1728,
        "numPointLights" to 2240,
        "pointLightPos[0]" to 2256,
        "pointLightColor[0]" to 2768,
        "pointLightAttenuation[0]" to 3280,
        "numShadows" to 3792,
        "bsps[0]" to 3808,
        "cascade[0]" to 4128,
        "yMin[0]" to 4208,
        "yMax[0]" to 4288,
        "shadowMode[0]" to 4368,
        "f1[0]" to 4448,
        "i1[0]" to 4528
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