package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.gl.GL.glBlendFunc
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glClearDepth
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glDepthFunc
import com.zakgof.korender.impl.gl.GL.glDepthMask
import com.zakgof.korender.impl.gl.GL.glDisable
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_CULL_FACE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.gl.GLConstants.GL_ONE
import com.zakgof.korender.impl.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import com.zakgof.korender.math.ColorRGBA

internal class GlState {

    private class Key<T>(val ordinal: Int, val setter: (T) -> Unit)

    private val CLEAR_COLOR = Key<ColorRGBA>(0) { glClearColor(it.r, it.g, it.b, it.a) }
    private val CLEAR_DEPTH = Key<Float>(1) { glClearDepth(it) }
    private val CULL_FACE = Key<Boolean>(2) { toggle(GL_CULL_FACE, it) }
    private val CULL_FACE_MODE = Key<Int>(3) { glCullFace(it) }
    private val BLEND = Key<Boolean>(4) { toggle(GL_BLEND, it) }
    private val BLEND_FUNC = Key<Pair<Int, Int>>(5) { glBlendFunc(it.first, it.second) }
    private val DEPTH_FUNC = Key<Int>(6) { glDepthFunc(it) }
    private val DEPTH_MASK = Key<Boolean>(7) { glDepthMask(it) }
    private val DEPTH_TEST = Key<Boolean>(8) { toggle(GL_DEPTH_TEST, it) }

    private val keys = arrayOf(
        CLEAR_COLOR,
        CLEAR_DEPTH,
        CULL_FACE,
        CULL_FACE_MODE,
        BLEND,
        BLEND_FUNC,
        DEPTH_FUNC,
        DEPTH_MASK,
        DEPTH_TEST,
    )

    private fun toggle(mode: Int, value: Boolean) = if (value) glEnable(mode) else glDisable(mode)

    private val defaults = arrayOf(
        ColorRGBA.Black,
        1.0f,
        true,
        GL_BACK,
        true,
        GL_ONE to GL_ONE_MINUS_SRC_ALPHA,
        GL_LEQUAL,
        true,
        true
    )

    private val actual = Array<Any?>(defaults.size) { null }

    fun set(block: StateContext.() -> Unit) {
        val context = StateContext()
        context.apply(block)
        context.target.forEachIndexed { i, v ->
            if (actual[i] != v) {
                (keys[i] as Key<Any>).setter(v)
                actual[i] = v
            }
        }
    }

    internal inner class StateContext {

        internal val target = defaults.copyOf()

        fun clearColor(c: ColorRGBA) = put(CLEAR_COLOR, c)
        fun clearDepth(d: Float) = put(CLEAR_DEPTH, d)
        fun cullFace(v: Boolean) = put(CULL_FACE, v)
        fun cullFaceMode(glValue: Int) = put(CULL_FACE_MODE, glValue)
        fun blend(v: Boolean) = put(BLEND, v)
        fun depthFunc(glValue: Int) = put(DEPTH_FUNC, glValue)
        fun depthMask(v: Boolean) = put(DEPTH_MASK, v)
        fun depthTest(v: Boolean) = put(DEPTH_TEST, v)
        fun blendFunc(sfactor: Int, dfactor: Int) = put(BLEND_FUNC, sfactor to dfactor)

        private fun put(key: Key<*>, value: Any) {
            target[key.ordinal] = value
        }
    }
}