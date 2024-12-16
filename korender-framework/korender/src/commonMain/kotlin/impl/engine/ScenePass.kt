package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.gl.GL.glBlendFunc
import com.zakgof.korender.gl.GL.glClear
import com.zakgof.korender.gl.GL.glClearColor
import com.zakgof.korender.gl.GL.glCullFace
import com.zakgof.korender.gl.GL.glDepthFunc
import com.zakgof.korender.gl.GL.glDepthMask
import com.zakgof.korender.gl.GL.glEnable
import com.zakgof.korender.gl.GL.glViewport
import com.zakgof.korender.gl.GLConstants.GL_BACK
import com.zakgof.korender.gl.GLConstants.GL_BLEND
import com.zakgof.korender.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.gl.GLConstants.GL_CULL_FACE
import com.zakgof.korender.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import com.zakgof.korender.gl.GLConstants.GL_SRC_ALPHA
import com.zakgof.korender.uniforms.UniformSupplier

internal class ScenePass(private val inventory: Inventory, private val camera: Camera, private val width: Int, private val height: Int, passDeclaration: PassDeclaration, shadowCascades: Int) {

    val touchBoxes = mutableListOf<Scene.TouchBox>()

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    init {
        passDeclaration.renderables.forEach {
            val renderable = Renderable.create(inventory, it, camera, false, shadowCascades)
            when (it.bucket) {
                Bucket.OPAQUE -> opaques.add(renderable)
                Bucket.SKY -> skies.add(renderable)
                Bucket.TRANSPARENT -> transparents.add(renderable)
                Bucket.SCREEN -> screens.add(renderable)
            }
        }
        val guiRenderers = passDeclaration.guis.map { GuiRenderer(inventory, width, height, it) }
        screens.addAll(guiRenderers.flatMap { it.renderables })
        touchBoxes.addAll(guiRenderers.flatMap { it.touchBoxes })
    }

    fun render(uniformDecorator: (UniformSupplier) -> UniformSupplier) {
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthFunc(GL_LEQUAL)
        glClearColor(0f, 0f, 0f, 0f)
        glViewport(0, 0, width, height)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        opaques.forEach { it.render(uniformDecorator) }
        skies.forEach { it.render(uniformDecorator) }
        screens.forEach { it.render(uniformDecorator) }
        glDepthMask(false)
        transparents.sortedByDescending { (camera.mat4 * it.transform.offset()).z }
            .forEach { it.render(uniformDecorator) }
        glDepthMask(true)

    }
}