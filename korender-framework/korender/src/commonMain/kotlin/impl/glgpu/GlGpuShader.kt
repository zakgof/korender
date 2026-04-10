package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.engine.NoTexUnitsAvailableException
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.engine.ShaderServices
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

internal class UniformBlock(
    val shaderBlockIndex: Int,
    val size: Int,
    val bindings: List<CompiledBlockBinding>,
)

internal sealed interface UniformGetter<T> {

    fun writeTo(buffer: NativeByteBuffer, obj: T, missing: () -> Unit) {}

    fun writeTo(shader: GlGpuShader, location: GLUniformLocation, obj: T, missing: () -> Unit, loader: (Any?) -> GlBindableTexture, rk: ResultKeeper?): Boolean = true

    @Suppress("UNCHECKED_CAST")
    fun <T, V> safe(getter: (T) -> V?, obj: T, missing: () -> Unit, consumer: (V) -> Unit) {
        val v = getter(obj)
        if (v != null) consumer(v) else missing()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, V> safeBool(getter: (T) -> V?, obj: T, missing: () -> Unit, consumer: (V) -> Boolean): Boolean {
        return getter(obj as T)
            ?.let { consumer(it) }
            ?: false
    }
}

internal class TextureGetter<T>(private val f: (T) -> Any?) : UniformGetter<T> {
    override fun writeTo(shader: GlGpuShader, location: GLUniformLocation, obj: T, missing: () -> Unit, loader: (Any?) -> GlBindableTexture, rk: ResultKeeper?): Boolean =
        safeBool(f, obj, missing) { v ->
            val texture = loader(v)
            if (texture == NotYetLoadedTexture) {
                rk?.fail()
                val unit = shader.shaderServices.textureBindingCache.bind(shader.shaderServices.zeroTex)
                shader.uniformI(location, unit)
                false
            } else {
                val unit = shader.shaderServices.textureBindingCache.bind(texture)
                shader.uniformI(location, unit)
                true
            }
        }
}

internal class TextureListGetter<T>(private val f: (T) -> GlGpuTextureList) : UniformGetter<T> {
    override fun writeTo(shader: GlGpuShader, location: GLUniformLocation, obj: T, missing: () -> Unit, loader: (Any?) -> GlBindableTexture, rk: ResultKeeper?): Boolean =
        safeBool(f, obj, missing) { v ->
            val units = (0 until v.totalNum)
                .map {
                    val tex = if (it < v.textures.size) v.textures[it] else null
                    shader.shaderServices.textureBindingCache.bind(tex ?: shader.shaderServices.zeroTex)
                }
            shader.uniformIV(location, units)
            true
        }
}

internal class ShadowTextureListGetter<T>(private val f: (T) -> GlGpuShadowTextureList) : UniformGetter<T> {
    override fun writeTo(shader: GlGpuShader, location: GLUniformLocation, obj: T, missing: () -> Unit, loader: (Any?) -> GlBindableTexture, rk: ResultKeeper?): Boolean =
        safeBool(f, obj, missing) { v ->
            val units = (0 until v.totalNum)
                .map {
                    val tex = if (it < v.textures.size) v.textures[it] else null
                    shader.shaderServices.textureBindingCache.bind(tex ?: shader.shaderServices.zeroShadowTex)
                }
            shader.uniformIV(location, units)
            true
        }
}

internal class GlGpuShader(
    private val name: String,
    vertexShaderText: String,
    fragmentShaderText: String,
    vertDebugInfo: (String) -> String,
    fragDebugInfo: (String) -> String,
    private val uniformPack: UniformPack,
    val shaderServices: ShaderServices
) : AutoCloseable {

    private val programHandle = glCreateProgram()
    private val vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER)
    private val shaderUniformBlock: UniformBlock?
    private val uniformBindings: List<CompiledUniformBinding>

    private val uniformCache = mutableMapOf<GLUniformLocation, Int>()
    private val uniformArrayCache = mutableMapOf<GLUniformLocation, List<Int>>()

    internal inner class CompiledUniformBinding(
        val location: GLUniformLocation,
        val name: String,
        val supplierIndex: Int,
        val getter: UniformGetter<Any>,
    ) {
        fun write(uniformPack: UniformPack, loader: (Any?) -> GlBindableTexture, materialName: String, rk: ResultKeeper?): Boolean {
            val obj = uniformPack[supplierIndex]!!
            return getter.writeTo(
                this@GlGpuShader, location, obj,
                { throw KorenderException("Material $materialName does not provide uniform $name") },
                loader, rk
            )
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

    @Suppress("UNCHECKED_CAST")
    private fun fetchUniformBlockBindings(blockIndex: Int): List<CompiledBlockBinding> {
        val uniformCount = IntArray(1)
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, uniformCount)

        val uniformIndices = IntArray(uniformCount[0])
        glGetActiveUniformBlockiv(programHandle, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformIndices)

        val uniformOffsets = IntArray(uniformCount[0])
        glGetActiveUniformsiv(programHandle, uniformIndices, GL_UNIFORM_OFFSET, uniformOffsets)

        return (0 until uniformCount[0]).map { i ->
            val name = glGetActiveUniformName(programHandle, uniformIndices[i])
            val index = uniformPack.indices.firstOrNull { uniformPack[it]?.uniform(name) != null }
            if (index == null)
                throw KorenderException("Uniform $name not declared in materials for shader $this")
            CompiledBlockBinding(uniformOffsets[i], name, index, uniformPack[index]!!.uniform(name)!! as UniformGetter<Any>)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchUniforms(): List<CompiledUniformBinding> {
        val numUniforms = glGetProgrami(programHandle, GL_ACTIVE_UNIFORMS)
        return (0 until numUniforms).mapNotNull { i ->
            val name: String = glGetActiveUniform(programHandle, i)
            val location = glGetUniformLocation(programHandle, name)
            location?.let {
                val index = uniformPack.indices.firstOrNull { uniformPack[it]?.uniform(name) != null }
                if (index == null)
                    throw KorenderException("Uniform $name not declared in materials for shader $this")
                CompiledUniformBinding(location, name, index, uniformPack[index]!!.uniform(name)!! as UniformGetter<Any>)
            }
        }
    }

    override fun close() {
        println("Destroying GPU Shader [$name] : $programHandle")
        glDeleteShader(vertexShaderHandle)
        glDeleteShader(fragmentShaderHandle)
        glDeleteProgram(programHandle)
    }

    fun render(uniformPack: UniformPack, loader: (Any?) -> GlBindableTexture, mesh: GlGpuMesh, rk: ResultKeeper?) {
        shaderServices.uboHolder.populate(uniformPack, shaderUniformBlock, this.toString(), rk) { binding, _ ->
            glUseProgram(programHandle)
            shaderUniformBlock?.let { glUniformBlockBinding(programHandle, it.shaderBlockIndex, binding) }
            // println("---------- Draw call: $this -------------")
            shaderServices.textureBindingCache.nextDraw()
            if (bindUniforms(uniformPack, loader, rk)) {
                mesh.render()
            } else {
                rk?.fail()
            }
        }

    }

    private fun bindUniforms(uniformPack: UniformPack, loader: (Any?) -> GlBindableTexture, rk: ResultKeeper?): Boolean {
        try {
            uniformBindings.forEach { binding ->
                val res = binding.write(uniformPack, loader, toString(), rk)
                if (!res) {
                    println("Skipping shader rendering because texture [${binding.name}] not loaded")
                    return false
                }
            }
            return true
        } catch (_: NoTexUnitsAvailableException) {
            val textureList = uniformBindings
                .filter { it.getter is TextureGetter || it.getter is TextureListGetter || it.getter is ShadowTextureListGetter }
                .joinToString("") { " - ${it.name}\n" }
            throw KorenderException("No texture units available for $this:\nTexture uniforms:\n$textureList")
        }
    }

    override fun toString() = name

    fun uniformI(location: GLUniformLocation, value: Int) {
        if (uniformCache[location] != value) {
            glUniform1i(location, value)
            uniformCache[location] = value
        }
    }

    fun uniformIV(location: GLUniformLocation, value: List<Int>) {
        if (uniformArrayCache[location] != value) {
            glUniform1iv(location, *value.toIntArray())
            uniformArrayCache[location] = value
        }
    }
}

internal data class GlGpuTextureList(val textures: List<GlGpuTexture?>, val totalNum: Int)

internal data class GlGpuShadowTextureList(val textures: List<GlGpuTexture?>, val totalNum: Int)