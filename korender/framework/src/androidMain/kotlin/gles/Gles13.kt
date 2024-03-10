package com.zakgof.korender.gles

import android.opengl.GLES20
import com.zakgof.korender.gl.IGL13

object Gles13 : IGL13 {
    override fun glActiveTexture(texture: Int) = GLES20.glActiveTexture(texture)
}
