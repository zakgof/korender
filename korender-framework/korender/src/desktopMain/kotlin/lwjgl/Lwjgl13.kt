package com.zakgof.korender.lwjgl

import org.lwjgl.opengl.GL13

class Lwjgl13 : com.zakgof.korender.impl.gl.IGL13 {
    override fun glActiveTexture(texture: Int) {
        GL13.glActiveTexture(texture)
    }
}
