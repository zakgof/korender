package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.buffer.BufferData
import com.zakgof.korender.buffer.Floater
import com.zakgof.korender.gl.GL.glBindBuffer
import com.zakgof.korender.gl.GL.glBufferData
import com.zakgof.korender.gl.GL.glDeleteBuffers
import com.zakgof.korender.gl.GL.glDrawElements
import com.zakgof.korender.gl.GL.glGenBuffers
import com.zakgof.korender.gl.GLConstants.GL_ARRAY_BUFFER
import com.zakgof.korender.gl.GLConstants.GL_DYNAMIC_DRAW
import com.zakgof.korender.gl.GLConstants.GL_ELEMENT_ARRAY_BUFFER
import com.zakgof.korender.gl.GLConstants.GL_STATIC_DRAW
import com.zakgof.korender.gl.GLConstants.GL_TRIANGLES
import com.zakgof.korender.gl.GLConstants.GL_UNSIGNED_INT
import com.zakgof.korender.gl.GLConstants.GL_UNSIGNED_SHORT
import com.zakgof.korender.impl.geometry.Attribute

internal class GlGpuMesh(
    private val name: String,
    val attrs: List<Attribute>,
    val vertexSize: Int,
    isDynamic: Boolean = false,
    private val isLongIndex: Boolean = false
) : AutoCloseable {

    private val vbHandle = glGenBuffers()
    private val ibHandle = glGenBuffers()
    private val usage: Int = if (isDynamic) GL_DYNAMIC_DRAW else GL_STATIC_DRAW

    private var vertices: Int = -1
    private var indices: Int = -1

    init {
        println("Creating GPU Mesh [$name] $vbHandle/$ibHandle")
    }

    fun render() =
        glDrawElements(
            GL_TRIANGLES,
            indices,
            if (isLongIndex) GL_UNSIGNED_INT else GL_UNSIGNED_SHORT,
            0
        )

    fun bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vbHandle)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibHandle)
    }

    fun update(vb: Floater, ib: BufferData<out Any>, vertices: Int, indices: Int) {
        this.vertices = vertices
        this.indices = indices
        bind()

        glBufferData(GL_ARRAY_BUFFER, vb, usage)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, usage)
    }

    override fun close() {
        println("Destroying GPU Mesh [$name] $vbHandle/$ibHandle")
        glDeleteBuffers(vbHandle)
        glDeleteBuffers(ibHandle)
    }
}