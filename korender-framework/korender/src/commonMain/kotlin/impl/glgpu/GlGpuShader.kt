package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.buffer.put
import com.zakgof.korender.impl.engine.GlTextureUnitCache
import com.zakgof.korender.impl.gl.GL.glAttachShader
import com.zakgof.korender.impl.gl.GL.glBindBuffer
import com.zakgof.korender.impl.gl.GL.glBindBufferBase
import com.zakgof.korender.impl.gl.GL.glBufferData
import com.zakgof.korender.impl.gl.GL.glBufferSubData
import com.zakgof.korender.impl.gl.GL.glCompileShader
import com.zakgof.korender.impl.gl.GL.glCreateProgram
import com.zakgof.korender.impl.gl.GL.glCreateShader
import com.zakgof.korender.impl.gl.GL.glDeleteBuffers
import com.zakgof.korender.impl.gl.GL.glDeleteProgram
import com.zakgof.korender.impl.gl.GL.glDeleteShader
import com.zakgof.korender.impl.gl.GL.glGenBuffers
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
import com.zakgof.korender.impl.gl.GLBuffer
import com.zakgof.korender.impl.gl.GLConstants.GL_ACTIVE_UNIFORMS
import com.zakgof.korender.impl.gl.GLConstants.GL_COMPILE_STATUS
import com.zakgof.korender.impl.gl.GLConstants.GL_DYNAMIC_DRAW
import com.zakgof.korender.impl.gl.GLConstants.GL_FRAGMENT_SHADER
import com.zakgof.korender.impl.gl.GLConstants.GL_LINK_STATUS
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BLOCK_DATA_SIZE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_BUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_UNIFORM_OFFSET
import com.zakgof.korender.impl.gl.GLConstants.GL_VERTEX_SHADER
import com.zakgof.korender.impl.gl.GLUniformLocation
import com.zakgof.korender.impl.material.NotYetLoadedTexture
import com.zakgof.korender.impl.material.ShaderDebugInfo
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat3
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: ShaderDebugInfo,
    fragDebugInfo: ShaderDebugInfo,
    private val zeroTex: GlGpuTexture,
    private val zeroShadowTex: GlGpuTexture
) : AutoCloseable {
    private val programHandle = glCreateProgram()
    private val vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER)

    private val ubo: GLBuffer
    private val uboBuffer: NativeByteBuffer
    private val uniformOffsets: Map<String, Int>
    private val uniformLocations: Map<String, GLUniformLocation>
    private val uniformCache = mutableMapOf<String, Any>()

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
            errorLog += "\n > Vertex shader log\n" + vertDebugInfo.decorate(vertexLog) + "\n"
        }

        val fragmentLog: String = glGetShaderInfoLog(fragmentShaderHandle)
        if (fragmentLog.isNotEmpty()) {
            errorLog += "\n >> Fragment shader log\n" + fragDebugInfo.decorate(fragmentLog) + "\n"
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
        val blockIndex = glGetUniformBlockIndex(programHandle, "Uniforms")
        uboBuffer = createUboBuffer(blockIndex)
        ubo = createUbo(blockIndex)
        uniformOffsets = fetchUniformBlocks(blockIndex)
        uniformLocations = fetchUniforms()
    }

    private fun createUbo(blockIndex: Int): GLBuffer {
        val ubo = glGenBuffers()
        glBindBuffer(GL_UNIFORM_BUFFER, ubo);
        glBufferData(GL_UNIFORM_BUFFER, uboBuffer, GL_DYNAMIC_DRAW)

        glUniformBlockBinding(programHandle, blockIndex, 0)
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo)

        return ubo
    }

    private fun createUboBuffer(blockIndex: Int): NativeByteBuffer {
        val blockSize = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, blockSize);
        return NativeByteBuffer(blockSize[0])
    }

    private fun fetchUniformBlocks(blockIndex: Int): Map<String, Int> {
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
        glDeleteBuffers(ubo)
        glDeleteShader(vertexShaderHandle)
        glDeleteShader(fragmentShaderHandle)
        glDeleteProgram(programHandle)
    }

    fun render(uniforms: (String) -> Any?, mesh: GlGpuMesh, textureUnitCache: GlTextureUnitCache): Boolean {
        glUseProgram(programHandle)
        var success = bindUniforms(uniforms, textureUnitCache)
        success = success or bindUbo(uniforms)
        if (success) {
            mesh.render()
        }
        return success
    }

    private fun bindUniforms(uniforms: (String) -> Any?, textureUnitCache: GlTextureUnitCache): Boolean {
        var result = true
        uniformLocations.forEach {
            val uniformValue = requireNotNull(uniforms(it.key)) { "Material ${toString()} does not provide value for the uniform ${it.key}" }
            if (uniformValue == NotYetLoadedTexture) {
                println("Skipping shader rendering because texture [${it.key}] not loaded")
                result = false
            }
            if (result) {
                bind(it.key, uniformValue, it.value, textureUnitCache)
            }
        }
        // glActiveTexture(GL_TEXTURE0)
        // glBindTexture(GL_TEXTURE_2D, null)
        return result
    }

    private fun bind(name: String, value: Any, location: GLUniformLocation, textureUnitCache: GlTextureUnitCache) {
        when (value) {

            is GLBindableTexture -> {
                val unit = textureUnitCache.bind(value)
                if (unit != uniformCache[name]) {
                    uniformCache[name] = unit
                    glUniform1i(location, unit)
                }
            }

            is GlGpuTextureList -> {
                val units = (0 until value.totalNum)
                    .map {
                        val tex = if (it < value.textures.size) value.textures[it] else null
                        textureUnitCache.bind(tex ?: zeroTex)
                    }
                if (units != uniformCache[name]) {
                    uniformCache[name] = units
                    glUniform1iv(location, *units.toIntArray())
                }
            }

            is GlGpuShadowTextureList -> {
                val units = (0 until value.totalNum)
                    .map {
                        val tex = if (it < value.textures.size) value.textures[it] else null
                        textureUnitCache.bind(tex ?: zeroShadowTex)
                    }
                if (units != uniformCache[name]) {
                    uniformCache[name] = units
                    glUniform1iv(location, *units.toIntArray())
                }
            }

            else -> {
                throw KorenderException("Unsupported uniform value $value of type ${value::class} for uniform $name")
            }

        }
    }

    private fun bindUbo(uniforms: (String) -> Any?): Boolean {
        uniformOffsets.forEach {
            val uniformValue = requireNotNull(uniforms(it.key)) { "Material ${toString()} does not provide value for the blocked uniform ${it.key}" }
            populateUbo(it.key, uniformValue, it.value)
        }
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo)
        glBindBuffer(GL_UNIFORM_BUFFER, ubo)
        glBufferSubData(GL_UNIFORM_BUFFER, 0L, uboBuffer.rewind())
        return true
    }

    private fun populateUbo(name: String, value: Any, offset: Int) {
        uboBuffer.position(offset)
        when (value) {
            is Int -> uboBuffer.put(value)
            is Float -> uboBuffer.put(value)
            is Vec2 -> {
                uboBuffer.put(value.x)
                uboBuffer.put(value.y)
            }

            is Vec3 -> uboBuffer.put(value)
            is ColorRGB -> {
                uboBuffer.put(value.r)
                uboBuffer.put(value.g)
                uboBuffer.put(value.b)
            }

            is ColorRGBA -> {
                uboBuffer.put(value.r)
                uboBuffer.put(value.g)
                uboBuffer.put(value.b)
                uboBuffer.put(value.a)
            }

            is Mat3 -> uboBuffer.put(value.asArray()) // TODO: padding
            is Mat4 -> uboBuffer.put(value.asArray())

            is IntList -> value.values.forEach {
                uboBuffer.put(it)
            }

            is FloatList -> value.values.forEach {
                uboBuffer.put(it)
            }

            is Vec3List -> value.values.forEach {
                uboBuffer.put(it.x)
                uboBuffer.put(it.y)
                uboBuffer.put(it.z)
            }

            is Color3List -> value.values.forEach {
                uboBuffer.put(it.r)
                uboBuffer.put(it.g)
                uboBuffer.put(it.b)
            }

            is Color4List -> value.values.forEach {
                uboBuffer.put(it.r)
                uboBuffer.put(it.g)
                uboBuffer.put(it.b)
                uboBuffer.put(it.a)
            }

            is Mat4List -> value.matrices.forEach {
                uboBuffer.put(it.asArray())
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