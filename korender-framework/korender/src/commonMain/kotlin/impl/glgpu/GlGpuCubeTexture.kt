package com.zakgof.korender.impl.glgpu

import com.zakgof.korender.impl.gl.GL
import com.zakgof.korender.impl.gl.GL.glActiveTexture
import com.zakgof.korender.impl.gl.GL.glBindTexture
import com.zakgof.korender.impl.gl.GL.glGenTextures
import com.zakgof.korender.impl.gl.GL.glTexImage2D
import com.zakgof.korender.impl.gl.GL.glTexParameteri
import com.zakgof.korender.impl.gl.GLConstants
import com.zakgof.korender.impl.gl.GLConstants.GL_CLAMP_TO_EDGE
import com.zakgof.korender.impl.gl.GLConstants.GL_LINEAR
import com.zakgof.korender.impl.gl.GLConstants.GL_RGB
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_X
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_POSITIVE_Z
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MAG_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_MIN_FILTER
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_R
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_S
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_WRAP_T
import com.zakgof.korender.impl.gl.GLConstants.GL_UNSIGNED_BYTE
import com.zakgof.korender.impl.gl.GLTexture
import com.zakgof.korender.impl.image.InternalImage

internal class GlGpuCubeTexture(
    imageNx: InternalImage,
    imageNy: InternalImage,
    imageNz: InternalImage,
    imagePx: InternalImage,
    imagePy: InternalImage,
    imagePz: InternalImage,
) : AutoCloseable {

    val glHandle: GLTexture = glGenTextures()

    init {
        println("Creating GPU Cube Texture $this")

        glBindTexture(GL_TEXTURE_CUBE_MAP, glHandle)

        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_RGB, imageNx.width, imageNx.height, 0, GL_RGB, GL_UNSIGNED_BYTE, imageNx.bytes)
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_RGB, imageNy.width, imageNy.height, 0, GL_RGB, GL_UNSIGNED_BYTE, imageNy.bytes)
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_RGB, imageNz.width, imageNz.height, 0, GL_RGB, GL_UNSIGNED_BYTE, imageNz.bytes)
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_RGB, imagePx.width, imagePx.height, 0, GL_RGB, GL_UNSIGNED_BYTE, imagePx.bytes)
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_RGB, imagePy.width, imagePy.height, 0, GL_RGB, GL_UNSIGNED_BYTE, imagePy.bytes)
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_RGB, imagePz.width, imagePz.height, 0, GL_RGB, GL_UNSIGNED_BYTE, imagePz.bytes)

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

        glBindTexture(GL_TEXTURE_CUBE_MAP, null)
    }

    fun bind(unit: Int) {
        glActiveTexture(GLConstants.GL_TEXTURE0 + unit)
        glBindTexture(GLConstants.GL_TEXTURE_CUBE_MAP, glHandle)
    }

    override fun close() {
        println("Destroying GPU Cube Texture $this")
        GL.glDeleteTextures(glHandle)
    }

    override fun toString() = "$glHandle"
}