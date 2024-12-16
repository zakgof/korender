package com.zakgof.korender.gles

import android.opengl.GLES20

object Gles13 : com.zakgof.korender.impl.gl.IGL13 {
    override fun glActiveTexture(texture: Int) = GLES20.glActiveTexture(texture)
}
