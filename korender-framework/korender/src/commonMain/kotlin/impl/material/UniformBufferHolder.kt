package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.gl.GL.glGetInteger
import com.zakgof.korender.impl.gl.GLConstants.GL_MAX_UNIFORM_BLOCK_SIZE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
import com.zakgof.korender.impl.glgpu.CompiledBlockBinding
import com.zakgof.korender.impl.glgpu.GlGpuUniformBuffer
import com.zakgof.korender.impl.glgpu.UniformBlock
import com.zakgof.korender.impl.glgpu.UniformGetter
import com.zakgof.korender.impl.glgpu.UniformPack

internal class UniformBufferHolder {

    private val frameUbo = GlGpuUniformBuffer(4640)

    private val frameBindings: List<Pair<String, Int>> = listOf(
        "cameraPos" to 0,
        "cameraDir" to 16,
        "view" to 32,
        "projectionWidth" to 96,
        "projectionHeight" to 100,
        "projectionNear" to 104,
        "projectionFar" to 108,
        "screenWidth" to 112,
        "screenHeight" to 116,
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

    private val compiledUniformBindings = mutableListOf<CompiledBlockBinding>()

    private val bufferOffsetAlignment = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT)
    private val shaderUboSize = glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE).coerceIn(0, 65536)

    private val shaderUbo = GlGpuUniformBuffer(shaderUboSize)

    private val renderQueue = mutableListOf<RenderItem>()
    private var bufferShift = 0

    init {
        frameUbo.bindBase(0)
    }

    @Suppress("UNCHECKED_CAST")
    fun populateFrame(uniformPack: UniformPack, ignoreMissing: Boolean = false) {
        if (compiledUniformBindings.isEmpty()) {
            compiledUniformBindings += frameBindings.map { pair ->
                val name = pair.first
                val index = uniformPack.indices.firstOrNull { uniformPack[it]?.uniform(name) != null }
                val getter = uniformPack[index!!]!!.uniform(name) ?: throw KorenderException("Uniform $name not declared in materials for shader $this")
                CompiledBlockBinding(pair.second, name, index, getter as UniformGetter<Any>)
            }
        }
        frameUbo.populate(uniformPack, 0, compiledUniformBindings, "FrameContext", ignoreMissing)
        frameUbo.upload()
    }

    fun populate(
        uniformPack: UniformPack,
        uniformBlock: UniformBlock?,
        materialName: String,
        rk: ResultKeeper?,
        render: (ResultKeeper?) -> Unit,
    ) {
        val renderItem = if (uniformBlock != null) {
            if (shaderUboSize - bufferShift < uniformBlock.size) {
                flush(rk)
            }
            shaderUbo.populate(uniformPack, bufferShift, uniformBlock.bindings, materialName)
            val ri = RenderItem(render, bufferShift, uniformBlock.size)
            bufferShift = ((bufferShift + uniformBlock.size + bufferOffsetAlignment - 1) / bufferOffsetAlignment) * bufferOffsetAlignment
            ri
        } else {
            RenderItem(render, 0, 0)
        }
        renderQueue += renderItem
    }

    fun flush(rk: ResultKeeper?) {
        if (renderQueue.isNotEmpty()) {
            shaderUbo.upload()
            renderQueue.forEach { renderItem ->
                if (renderItem.size > 0) {
                    shaderUbo.bindRange(1, renderItem.shift, renderItem.size)
                }
                renderItem.render(rk)
            }
        }
        bufferShift = 0
        renderQueue.clear()
    }

    private class RenderItem(
        val render: (ResultKeeper?) -> Unit,
        val shift: Int,
        val size: Int
    )
}