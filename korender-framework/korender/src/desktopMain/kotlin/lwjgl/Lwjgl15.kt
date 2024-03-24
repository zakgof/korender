package com.zakgof.korender.lwjgl

import org.lwjgl.opengl.GL15
import java.nio.ByteBuffer

class Lwjgl15 : com.zakgof.korender.impl.gl.IGL15 {
    override fun glBindBuffer(target: Int, buffer: Int) = GL15.glBindBuffer(target, buffer)

    override fun glBufferData(target: Int, data: ByteBuffer, usage: Int) =
        GL15.glBufferData(target, data, usage)

    override fun glGenBuffers() = GL15.glGenBuffers()

    override fun glDeleteBuffers(buffer: Int) = GL15.glDeleteBuffers(buffer)
}
