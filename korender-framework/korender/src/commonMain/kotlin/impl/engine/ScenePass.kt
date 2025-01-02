package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.impl.gl.GL.glBlendFunc
import com.zakgof.korender.impl.gl.GL.glClear
import com.zakgof.korender.impl.gl.GL.glClearColor
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glDepthFunc
import com.zakgof.korender.impl.gl.GL.glDepthMask
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GL.glViewport
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_COLOR_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_CULL_FACE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import com.zakgof.korender.impl.gl.GLConstants.GL_SRC_ALPHA
import com.zakgof.korender.impl.gltf.GltfSceneBuilder

internal class ScenePass(
    private val inventory: Inventory,
    private val camera: Camera,
    private val width: Int,
    private val height: Int,
    passDeclaration: PassDeclaration,
    shadowCascades: Int,
    time: Float
) {

    val touchBoxes = mutableListOf<Scene.TouchBox>()

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()

    init {
        passDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { l ->
                passDeclaration.renderables += GltfSceneBuilder(inventory, it.gltfResource, l).build(time)
            }
        }
        passDeclaration.renderables.forEach {
            val renderable = Renderable.create(inventory, it, camera, false, shadowCascades)
            renderable?.let { r ->
                when (it.bucket) {
                    Bucket.OPAQUE -> opaques.add(r)
                    Bucket.SKY -> skies.add(r)
                    Bucket.TRANSPARENT -> transparents.add(r)
                    Bucket.SCREEN -> screens.add(r)
                }
            }
        }
        val guiRenderers = passDeclaration.guis.map { GuiRenderer(inventory, width, height, it) }
        screens.addAll(guiRenderers.flatMap { it.renderables })
        touchBoxes.addAll(guiRenderers.flatMap { it.touchBoxes })
    }

    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        glClearColor(0.05f, 0.05f, 0.1f, 1.0f)
        glViewport(0, 0, width, height)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthFunc(GL_LEQUAL)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        opaques.forEach { it.render(contextUniforms, fixer) }
        skies.forEach { it.render(contextUniforms, fixer) }
        screens.forEach { it.render(contextUniforms, fixer) }
        glDepthMask(false)
        transparents.sortedByDescending { (camera.mat4 * it.transform.offset()).z }
            .forEach { it.render(contextUniforms, fixer) }
        glDepthMask(true)
    }
}