package com.zakgof.korender.impl.material

import com.zakgof.korender.impl.glgpu.GlGpuUniformBuffer

internal class UniformBufferHolder {

    val frameUbo = GlGpuUniformBuffer(4608)

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

    private val shaderUboSize = 16384
    val shaderUbo = GlGpuUniformBuffer(shaderUboSize)
    private var bufferShift = 0

    private val renderQueue = mutableListOf<RenderItem>()

    fun populateFrame(uniforms: (String) -> Any?, binding: Int, ignoreMissing: Boolean = false) =
        frameUbo.populate(uniforms, 0, frameOffsets, binding, "FrameContect", ignoreMissing)

    fun populate(
        uniforms: (String) -> Any?,
        size: Int?,
        offsets: Map<String, Int>?,
        binding: Int,
        materialName: String,
        render: () -> Boolean
    ) {
        size?.let {
            if (shaderUboSize - bufferShift < size) {
                flush()
            }
            shaderUbo.populate(uniforms, bufferShift, offsets!!, binding, materialName)
        }
        renderQueue += RenderItem(render, bufferShift, size ?: 0, binding)
        bufferShift = ((bufferShift + (size ?: 0) + 256 - 1) / 256) * 256 // TODO use GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
    }

    fun flush(): Boolean {
        if (renderQueue.isEmpty())
            return true
        shaderUbo.upload(bufferShift)
        var success = true
        renderQueue.forEach {
            shaderUbo.setShift(it.binding, it.shift, it.size)
            success = success or it.render()
        }
        bufferShift = 0
        renderQueue.clear()
        return success
    }



    private class RenderItem(
        val render: () -> Boolean,
        val shift: Int,
        val size: Int,
        val binding: Int
    )
}