package com.zakgof.korender.lwjgl

import com.zakgof.korender.gl.IGL13
import org.lwjgl.opengl.GL13

class Lwjgl13 : IGL13 {
    override fun glActiveTexture(texture: Int) {
        GL13.glActiveTexture(texture)
    }
}
