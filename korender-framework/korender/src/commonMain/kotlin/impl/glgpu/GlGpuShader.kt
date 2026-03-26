package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.buffer.NativeByteBuffer
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
import com.zakgof.korender.impl.material.UniformBufferHolder
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Mat4
import com.zakgof.korender.math.Vec3

internal class UniformBlock(
    val shaderBlockIndex: Int,
    val size: Int,
    val bindings: List<CompiledBlockBinding>
)

internal sealed interface UniformGetter<T> {

    fun writeTo(buffer: NativeByteBuffer, obj: Any, missingMessage: String?) {}

    fun writeTo(location: GLUniformLocation, currentTextureUnit: Int, obj: Any, missingMessage: String?, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture, loader: (Any?) -> GLBindableTexture): Int? = 0

    @Suppress("UNCHECKED_CAST")
    fun <V> safe(getter: (T) -> V, obj: Any, missingMessage: String?, consumer: (V) -> Unit) {
        getter(obj as T)?.let { consumer(it) } ?: missingMessage?.let { throw KorenderException(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <V> safeInt(getter: (T) -> V, obj: Any, missingMessage: String?, consumer: (V) -> Int?): Int? {
        return getter(obj as T)?.let { consumer(it) } ?: missingMessage?.let { throw KorenderException(it) } ?: 0
    }
}

internal class TextureGetter<T>(private val f: (T) -> Any?) : UniformGetter<T> {
    override fun writeTo(location: GLUniformLocation, currentTextureUnit: Int, obj: Any, missingMessage: String?, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture, loader: (Any?) -> GLBindableTexture) =
        safeInt(f, obj, missingMessage) { v ->
            val texture= loader(v)
            if (v == NotYetLoadedTexture) {
                null
            } else {
                texture.bind(currentTextureUnit)
                glUniform1i(location, currentTextureUnit)
                1
            }
        }
}

internal class TextureListGetter<T>(private val f: (T) -> GlGpuTextureList) : UniformGetter<T> {
    override fun writeTo(location: GLUniformLocation, currentTextureUnit: Int, obj: Any, missingMessage: String?, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture, loader: (Any?) -> GLBindableTexture) =
        safeInt(f, obj, missingMessage) { v ->
            val units = (0 until v.totalNum)
                .map {
                    val ctu = currentTextureUnit + it
                    val tex = if (it < v.textures.size) v.textures[it] else null
                    (tex ?: zeroTex).bind(ctu)
                    ctu
                }
            glUniform1iv(location, *units.toIntArray())
            v.totalNum
        }
}

internal class ShadowTextureListGetter<T>(private val f: (T) -> GlGpuShadowTextureList) : UniformGetter<T> {
    override fun writeTo(location: GLUniformLocation, currentTextureUnit: Int, obj: Any, missingMessage: String?, zeroTex: GlGpuTexture, zeroShadowTex: GlGpuTexture, loader: (Any?) -> GLBindableTexture) =
        safeInt(f, obj, missingMessage) { v ->
            val units = (0 until v.totalNum)
                .map {
                    val ctu = currentTextureUnit + it
                    val tex = if (it < v.textures.size) v.textures[it] else null
                    (tex ?: zeroShadowTex).bind(ctu)
                    ctu
                }
            glUniform1iv(location, *units.toIntArray())
            v.totalNum
        }
}

internal class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: (String) -> String,
    fragDebugInfo: (String) -> String,
    private val zeroTex: GlGpuTexture,
    private val zeroShadowTex: GlGpuTexture,
    private val uboHolder: UniformBufferHolder,
    private val uniformSuppliers: List<UniformSupplier>
) : AutoCloseable {

    private val programHandle = glCreateProgram()
    private val vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER)
    private val shaderUniformBlock: UniformBlock?
    private val uniformBindings: List<CompiledUniformBinding>


    internal inner class CompiledUniformBinding(
        val location: GLUniformLocation,
        val name: String,
        val supplierIndex: Int,
        val getter: UniformGetter<*>,
    ) {
        fun write(currentTextureUnit: Int, suppliers: List<UniformSupplier>, loader: (Any?) -> GLBindableTexture, materialName: String): Int? {
            val missingMessage = "Material $materialName does not provide uniform $name"
            val obj = suppliers[supplierIndex]
            return getter.writeTo(location, currentTextureUnit, obj, missingMessage, zeroTex, zeroShadowTex, loader)
        }
    }

    init {

        println("Creating GPU Shader [$name] : $programHandle")

//        println("----  Vertex shader  ----")
//        println(vertexShaderText)
//        println("---- Fragment shader ----")
//        println(fragmentShaderText)
//        println("-------------------------")

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

        val frameUboBlockIndex = glGetUniformBlockIndex(programHandle, "Frame")
        if (frameUboBlockIndex >= 0) {
            // dumpUboBlock(frameUboBlockIndex)
            glUniformBlockBinding(programHandle, frameUboBlockIndex, 0)
        }

        shaderUniformBlock = initShaderUniformBlock()
        uniformBindings = fetchUniforms()
    }

    private fun dumpUboBlock(blockIndex: Int) {

        val blockSize = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, blockSize)
        println("Size: ${blockSize[0]}")

        val uniformCount = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, uniformCount)

        val uniformIndices = IntArray(uniformCount[0])
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformIndices)

        val uniformOffsets = IntArray(uniformCount[0])
        glGetActiveUniformsiv(programHandle, uniformIndices, GL_UNIFORM_OFFSET, uniformOffsets)

        (0 until uniformCount[0]).forEach {
            val name = glGetActiveUniformName(programHandle, uniformIndices[it])
            println("\"$name\" to ${uniformOffsets[it]},")
        }
    }

    private fun initShaderUniformBlock(): UniformBlock? {
        val blockIndex = glGetUniformBlockIndex(programHandle, "Uniforms")
        if (blockIndex < 0)
            return null
        val blockSize = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, blockSize)
        return UniformBlock(blockIndex, blockSize[0], fetchUniformBlockBindings(blockIndex))
    }

    private fun fetchUniformBlockBindings(blockIndex: Int): List<CompiledBlockBinding> {
        val uniformCount = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, uniformCount)

        val uniformIndices = IntArray(uniformCount[0])
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformIndices)

        val uniformOffsets = IntArray(uniformCount[0])
        glGetActiveUniformsiv(programHandle, uniformIndices, GL_UNIFORM_OFFSET, uniformOffsets)

        return (0 until uniformCount[0]).map { i ->
            val name = glGetActiveUniformName(programHandle, uniformIndices[i])
            val index = uniformSuppliers.indices.firstOrNull { uniformSuppliers[it].uniform(name) != null }
            if (index == null)
                throw KorenderException("Uniform $name not declared in materials for shader $this")
            CompiledBlockBinding(uniformOffsets[i], name, index, uniformSuppliers[index].uniform(name)!!)
        }
    }

    private fun fetchUniforms(): List<CompiledUniformBinding> {
        val numUniforms = glGetProgrami(programHandle, GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).map { i ->
            val name: String = glGetActiveUniform(programHandle, i)
            val location = glGetUniformLocation(programHandle, name)
            val index = uniformSuppliers.indices.firstOrNull { uniformSuppliers[it].uniform(name) != null }
            if (index == null)
                throw KorenderException("Uniform $name not declared in materials for shader $this")
            CompiledUniformBinding(location!!, name, index, uniformSuppliers[index].uniform(name)!!)
        }
    }

    override fun close() {
        println("Destroying GPU Shader [$name] : $programHandle")
        glDeleteShader(vertexShaderHandle)
        glDeleteShader(fragmentShaderHandle)
        glDeleteProgram(programHandle)
    }

    fun render(uniformsSuppliers: List<UniformSupplier>, loader: (Any?) -> GLBindableTexture, mesh: GlGpuMesh) {
        uboHolder.populate(uniformsSuppliers, shaderUniformBlock, this.toString()) { binding ->
            glUseProgram(programHandle)
            shaderUniformBlock?.let { glUniformBlockBinding(programHandle, it.shaderBlockIndex, binding) }
            if (bindUniforms(uniformsSuppliers, loader)) {
                mesh.render()
                true
            } else
                false
        }

    }

    private fun bindUniforms(uniformSuppliers: List<UniformSupplier>, loader: (Any?) -> GLBindableTexture): Boolean {
        var currentTextureUnit = 1
        uniformBindings.forEach { binding ->
            val ret = binding.write(currentTextureUnit, uniformSuppliers, loader,toString())
            if (ret == null) {
                println("Skipping shader rendering because texture [${binding.name}] not loaded")
                return false
            }
            currentTextureUnit += ret
        }
        return true
    }

    override fun toString() = name
}

// Get rid of these
internal data class IntList(val values: List<Int>)

internal data class FloatList(val values: List<Float>)

internal data class Mat4List(val matrices: List<Mat4>)

internal data class Vec3List(val values: List<Vec3>)

internal data class Color4List(val values: List<ColorRGBA>)

internal data class Color3List(val values: List<ColorRGB>)

internal data class GlGpuTextureList(val textures: List<GlGpuTexture?>, val totalNum: Int)

internal data class GlGpuShadowTextureList(val textures: List<GlGpuTexture?>, val totalNum: Int)