package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.AttributeType
import com.zakgof.korender.IndexType
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.gl.GL.glBindBuffer
import com.zakgof.korender.impl.gl.GL.glBindVertexArray
import com.zakgof.korender.impl.gl.GL.glBufferData
import com.zakgof.korender.impl.gl.GL.glDeleteBuffers
import com.zakgof.korender.impl.gl.GL.glDeleteVertexArrays
import com.zakgof.korender.impl.gl.GL.glDrawArrays
import com.zakgof.korender.impl.gl.GL.glDrawElements
import com.zakgof.korender.impl.gl.GL.glEnableVertexAttribArray
import com.zakgof.korender.impl.gl.GL.glGenBuffers
import com.zakgof.korender.impl.gl.GL.glGenVertexArrays
import com.zakgof.korender.impl.gl.GL.glVertexAttribIPointer
import com.zakgof.korender.impl.gl.GL.glVertexAttribPointer
import com.zakgof.korender.impl.gl.GLConstants.GL_ARRAY_BUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_BYTE
import com.zakgof.korender.impl.gl.GLConstants.GL_DYNAMIC_DRAW
import com.zakgof.korender.impl.gl.GLConstants.GL_ELEMENT_ARRAY_BUFFER
import com.zakgof.korender.impl.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.impl.gl.GLConstants.GL_INT
import com.zakgof.korender.impl.gl.GLConstants.GL_SHORT
import com.zakgof.korender.impl.gl.GLConstants.GL_STATIC_DRAW
import com.zakgof.korender.impl.gl.GLConstants.GL_TRIANGLES
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_INT
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_SHORT

internal fun AttributeType.toGL(): Int = when (this) {
    AttributeType.Byte -> GL_UNSIGNED_BYTE
    AttributeType.Short -> GL_UNSIGNED_SHORT
    AttributeType.Int -> GL_UNSIGNED_INT
    AttributeType.SignedByte -> GL_BYTE
    AttributeType.SignedShort -> GL_SHORT
    AttributeType.SignedInt -> GL_INT
    AttributeType.Float -> GL_FLOAT
}

internal class GlGpuMesh(
    private val name: String,
    val attrs: List<MeshAttribute>,
    isDynamic: Boolean = false,
    private val indexType: IndexType
) : AutoCloseable {

    private val vao = glGenVertexArrays()
    private val vbos = attrs.map { glGenBuffers() }
    private val ebo = glGenBuffers()
    private val usage: Int = if (isDynamic) GL_DYNAMIC_DRAW else GL_STATIC_DRAW

    private var vertices: Int = -1
    private var indices: Int = -1

    init {
        println("Creating GPU Mesh [$name] $vao/$vbos/$ebo")
    }

    fun bind() = glBindVertexArray(vao)

    fun update(
        vb: List<NativeByteBuffer>,
        ib: NativeByteBuffer?,
        vertices: Int,
        indices: Int
    ) {
        this.vertices = vertices
        this.indices = indices
        glBindVertexArray(vao)

        attrs.forEachIndexed { index, attr ->
            val vbo = vbos[index]
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, vb[index], usage)

            if (attr.primitiveType == AttributeType.Float)
                glVertexAttribPointer(attr.location, attr.structSize, attr.primitiveType.toGL(), false, 0, 0)
            else {
                glVertexAttribIPointer(attr.location, attr.structSize, attr.primitiveType.toGL(), 0, 0)
            }
            glEnableVertexAttribArray(attr.location)
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        ib?.let {glBufferData(GL_ELEMENT_ARRAY_BUFFER, it, usage)}

        glBindVertexArray(null)
    }

    fun render() {
        glBindVertexArray(vao)
        if (indices <= 0) {
            glDrawArrays(GL_TRIANGLES, 0, vertices)
        } else {
            glDrawElements(
                GL_TRIANGLES,
                indices,
                when (indexType) {
                    IndexType.Byte -> GL_UNSIGNED_BYTE
                    IndexType.Short -> GL_UNSIGNED_SHORT
                    IndexType.Int -> GL_UNSIGNED_INT
                },
                0
            )
            // checkGlError("during glDrawElements")
        }
        glBindVertexArray(null)
    }

    override fun close() {
        println("Destroying GPU Mesh [$name] $vao/$vbos/$ebo")
        vbos.forEach { glDeleteBuffers(it) }
        glDeleteBuffers(ebo)
        glDeleteVertexArrays(vao)
    }
}