package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.buffer.BufferData
import com.zakgof.korender.gl.GL.glBindBuffer
import com.zakgof.korender.gl.GL.glBindVertexArray
import com.zakgof.korender.gl.GL.glBufferData
import com.zakgof.korender.gl.GL.glDeleteBuffers
import com.zakgof.korender.gl.GL.glDeleteVertexArrays
import com.zakgof.korender.gl.GL.glDrawElements
import com.zakgof.korender.gl.GL.glEnableVertexAttribArray
import com.zakgof.korender.gl.GL.glGenBuffers
import com.zakgof.korender.gl.GL.glGenVertexArrays
import com.zakgof.korender.gl.GL.glVertexAttribPointer
import com.zakgof.korender.gl.GLConstants.GL_ARRAY_BUFFER
import com.zakgof.korender.gl.GLConstants.GL_DYNAMIC_DRAW
import com.zakgof.korender.gl.GLConstants.GL_ELEMENT_ARRAY_BUFFER
import com.zakgof.korender.gl.GLConstants.GL_FLOAT
import com.zakgof.korender.gl.GLConstants.GL_STATIC_DRAW
import com.zakgof.korender.gl.GLConstants.GL_TRIANGLES
import com.zakgof.korender.gl.GLConstants.GL_UNSIGNED_INT
import com.zakgof.korender.gl.GLConstants.GL_UNSIGNED_SHORT
import com.zakgof.korender.impl.geometry.Attribute


internal class GlGpuMesh(
    private val name: String,
    val attrs: List<Attribute>,
    isDynamic: Boolean = false,
    private val isLongIndex: Boolean = false
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
        vb: List<BufferData<out Any>>,
        ib: BufferData<out Any>,
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
            glEnableVertexAttribArray(index);
            glVertexAttribPointer(index, attr.size, GL_FLOAT, false, 0, 0)

            println("Update attr data in GPU: ${attr.name} ${vb[index]}")
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, usage)

        println("Update index data in GPU: $ib ")

        glBindVertexArray(null)
    }

    fun render() {
        glBindVertexArray(vao)
        glDrawElements(
            GL_TRIANGLES,
            indices,
            if (isLongIndex) GL_UNSIGNED_INT else GL_UNSIGNED_SHORT,
            0
        )
        glBindVertexArray(null)
    }

    override fun close() {
        println("Destroying GPU Mesh [$name] $vao/$vbos/$ebo")
        vbos.forEach { glDeleteBuffers(it) }
        glDeleteBuffers(ebo)
        glDeleteVertexArrays(vao)
    }
}