package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.gl.GL.glAttachShader
import com.zakgof.korender.impl.gl.GL.glCompileShader
import com.zakgof.korender.impl.gl.GL.glCreateProgram
import com.zakgof.korender.impl.gl.GL.glCreateShader
import com.zakgof.korender.impl.gl.GL.glDeleteProgram
import com.zakgof.korender.impl.gl.GL.glDeleteShader
import com.zakgof.korender.impl.gl.GL.glGetActiveUniform
import com.zakgof.korender.impl.gl.GL.glGetActiveUniformBlockiv
import com.zakgof.korender.impl.gl.GL.glGetActiveUniformName
import com.zakgof.korender.impl.gl.GL.glGetActiveUniformsiv
import com.zakgof.korender.impl.gl.GL.glGetProgramInfoLog
import com.zakgof.korender.impl.gl.GL.glGetProgrami
import com.zakgof.korender.impl.gl.GL.glGetShaderInfoLog
import com.zakgof.korender.impl.gl.GL.glGetShaderi
import com.zakgof.korender.impl.gl.GL.glGetUniformBlockIndex
import com.zakgof.korender.impl.gl.GL.glGetUniformLocation
import com.zakgof.korender.impl.gl.GL.glLinkProgram
import com.zakgof.korender.impl.gl.GL.glShaderSource
import com.zakgof.korender.impl.gl.GL.glUniform1i
import com.zakgof.korender.impl.gl.GL.glUniform1iv
import com.zakgof.korender.impl.gl.GL.glUniformBlockBinding
import com.zakgof.korender.impl.gl.GL.glUseProgram
import com.zakgof.korender.impl.gl.GLConstants.GL_ACTIVE_UNIFORMS
import com.zakgof.korender.impl.gl.GLConstants.GL_COMPILE_STATUS
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAGMENT_SHADER
import com.zakgof.korender.impl.gl.GLConstants.GL_LINK_STATUS
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BLOCK_DATA_SIZE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_OFFSET
import com.zakgof.korender.impl.gl.GLConstants.GL_VERTEX_SHADER
import com.zakgof.korender.impl.gl.GLUniformLocation
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

internal class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: (String) -> String,
    fragDebugInfo: (String) -> String,
    private val zeroTex: GlGpuTexture,
    private val zeroShadowTex: GlGpuTexture,
    private val frameUbo: GlGpuUniformBuffer
) : AutoCloseable {
    private val programHandle = glCreateProgram()
    private val vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER)

    private val shaderUbo: GlGpuUniformBuffer?

    private val uniformLocations: Map<String, GLUniformLocation>

    init {

        println("Creating GPU Shader [$name] : $programHandle")

//        println("Vertex ====")
//        println(vertexShaderText)
//        println("Fragment ====")
//        println(fragmentShaderText)

        glShaderSource(vertexShaderHandle, vertexShaderText)
        glCompileShader(vertexShaderHandle)

        glShaderSource(fragmentShaderHandle, fragmentShaderText)
        glCompileShader(fragmentShaderHandle)

        glAttachShader(programHandle, vertexShaderHandle)
        glAttachShader(programHandle, fragmentShaderHandle)

        glLinkProgram(programHandle)

        var errorLog = "\n"
        val vertexLog: String = glGetShaderInfoLog(vertexShaderHandle)
        if (vertexLog.isNotEmpty()) {
            errorLog += "\n > Vertex shader log\n" + vertDebugInfo(vertexLog) + "\n"
        }

        val fragmentLog: String = glGetShaderInfoLog(fragmentShaderHandle)
        if (fragmentLog.isNotEmpty()) {
            errorLog += "\n >> Fragment shader log\n" + fragDebugInfo(fragmentLog) + "\n"
        }

        val programLog: String = glGetProgramInfoLog(programHandle)
        if (programLog.isNotEmpty()) {
            errorLog += "\n >> Program log [$name]\n${programLog.lines().joinToString("\n") { "       $it" }}\n"
        }

        if (glGetShaderi(vertexShaderHandle, GL_COMPILE_STATUS) == 0)
            throw KorenderException("Vertex shader compilation failure $errorLog")
        if (glGetShaderi(fragmentShaderHandle, GL_COMPILE_STATUS) == 0) {
            throw KorenderException("Fragment shader compilation failure $errorLog")
        } else if (glGetProgrami(programHandle, GL_LINK_STATUS) == 0) {
            throw KorenderException("Program linking failure $errorLog")
        } else if (vertexLog.isNotEmpty()) {
            throw KorenderException("Vertex shader compilation warnings $errorLog")
        } else if (fragmentLog.isNotEmpty()) {
            throw KorenderException("Fragment shader compilation warnings $errorLog")
        } else if (programLog.isNotEmpty()) {
            throw KorenderException("Program linking warnings $errorLog")
        }

        val shaderUboBlockIndex = glGetUniformBlockIndex(programHandle, "Uniforms")

        shaderUbo = if (shaderUboBlockIndex >= 0) createShaderUbo(shaderUboBlockIndex) else null
        shaderUbo?.let { attachUbo(shaderUbo, shaderUboBlockIndex, 1) }

        val contextUboBlockIndex = glGetUniformBlockIndex(programHandle, "Frame")
        // createShaderUbo(contextUboBlockIndex)
        if (contextUboBlockIndex >= 0) {
            attachUbo(frameUbo, contextUboBlockIndex, 0)
        }

        uniformLocations = fetchUniforms()
    }

    private fun createShaderUbo(blockIndex: Int): GlGpuUniformBuffer? {
        val blockSize = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, blockSize);
        val offsets = fetchUniformBlockOffsets(blockIndex)
        return if (offsets.isEmpty()) null else GlGpuUniformBuffer(blockSize[0], offsets)
    }

    private fun attachUbo(ubo: GlGpuUniformBuffer, blockIndex: Int, blockBinding: Int) {
        glUniformBlockBinding(programHandle, blockIndex, blockBinding)
        ubo.bindShader(blockBinding)
    }

    private fun fetchUniformBlockOffsets(blockIndex: Int): Map<String, Int> {
        val uniformCount = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, uniformCount)

        val uniformIndices = IntArray(uniformCount[0])
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformIndices)

        val uniformOffsets = IntArray(uniformCount[0])
        glGetActiveUniformsiv(programHandle, uniformIndices, GL_UNIFORM_OFFSET, uniformOffsets)

        return (0 until uniformCount[0]).associate {
            val name = glGetActiveUniformName(programHandle, uniformIndices[it])
            name to uniformOffsets[it]
        }
    }

    private fun fetchUniforms(): Map<String, GLUniformLocation> {
        val numUniforms = glGetProgrami(programHandle, GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).associate {
            val name: String = glGetActiveUniform(programHandle, it)
            val location = glGetUniformLocation(programHandle, name)
            name to location
        }.filterValues { it != null }
            .mapValues { it.value!! } // TODO ugly
    }

    override fun close() {
        println("Destroying GPU Shader [$name] : $programHandle")
        shaderUbo?.close()
        glDeleteShader(vertexShaderHandle)
        glDeleteShader(fragmentShaderHandle)
        glDeleteProgram(programHandle)
    }

    fun render(uniforms: (String) -> Any?, mesh: GlGpuMesh): Boolean {
        glUseProgram(programHandle)
        shaderUbo?.populate(uniforms, 1, this.toString())
        val success = bindUniforms(uniforms)
        if (success) {
            mesh.render()
        }
        return success
    }

    private fun bindUniforms(uniforms: (String) -> Any?): Boolean {
        var currentTextureUnit = 1
        uniformLocations.forEach {
            val uniformValue = requireNotNull(uniforms(it.key)) { "Material ${toString()} does not provide value for the uniform ${it.key}" }
            if (uniformValue == NotYetLoadedTexture) {
                println("Skipping shader rendering because texture [${it.key}] not loaded")
                return false
            }
            currentTextureUnit += bindUniform(it.key, uniformValue, it.value, currentTextureUnit)
        }
        // glActiveTexture(GL_TEXTURE0)
        // glBindTexture(GL_TEXTURE_2D, null)
        return true
    }

    private fun bindUniform(name: String, value: Any, location: GLUniformLocation, currentTextureUnit: Int): Int {
        when (value) {

            is GLBindableTexture -> {
                value.bind(currentTextureUnit)
                glUniform1i(location, currentTextureUnit)
                return 1
            }

            is GlGpuTextureList -> {
                val units = (0 until value.totalNum)
                    .map {
                        val ctu = currentTextureUnit + it
                        val tex = if (it < value.textures.size) value.textures[it] else null
                        (tex ?: zeroTex).bind(ctu)
                        ctu
                    }
                glUniform1iv(location, *units.toIntArray())
                return value.totalNum
            }

            is GlGpuShadowTextureList -> {
                val units = (0 until value.totalNum)
                    .map {
                        val ctu = currentTextureUnit + it
                        val tex = if (it < value.textures.size) value.textures[it] else null
                        (tex ?: zeroShadowTex).bind(ctu)
                        ctu
                    }
                glUniform1iv(location, *units.toIntArray())
                return value.totalNum
            }

            else -> {
                throw KorenderException("Unsupported uniform value $value of type ${value::class} for uniform $name")
            }

        }
    }

    override fun toString() = name
}

internal data class IntList(val values: List<Int>)

internal data class FloatList(val values: List<Float>)

internal data class Mat4List(val matrices: List<Mat4>)

internal data class Vec3List(val values: List<Vec3>)

internal data class Color4List(val values: List<ColorRGBA>)

internal data class Color3List(val values: List<ColorRGB>)

internal data class GlGpuTextureList(val textures: List<GlGpuTexture?>, val totalNum: Int)

internal data class GlGpuShadowTextureList(val textures: List<GlGpuTexture?>, val totalNum: Int)