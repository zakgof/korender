package com.zakgof.korender.impl.engine

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
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3

internal class ScenePass(
    private val inventory: Inventory,
    private val renderContext: RenderContext,
    passDeclaration: PassDeclaration,
    shadowCascades: Int,
    time: Float
) {

    val touchBoxes = mutableListOf<Scene.TouchBox>()

    private val opaques = mutableListOf<Renderable>()
    private val transparents = mutableListOf<Renderable>()
    private val skies = mutableListOf<Renderable>()
    private val screens = mutableListOf<Renderable>()
    val lightUniforms = mutableMapOf<String, Any?>()

    init {

        fillLightUniforms(passDeclaration)

        passDeclaration.gltfs.forEach {
            inventory.gltf(it)?.let { l ->
                passDeclaration.renderables += GltfSceneBuilder(inventory, it.gltfResource, l).build(time)
            }
        }
        passDeclaration.renderables.forEach {
            val renderable = Renderable.create(inventory, it, renderContext.camera, false, shadowCascades)
            renderable?.let { r ->
                when (it.bucket) {
                    Bucket.OPAQUE -> opaques.add(r)
                    Bucket.SKY -> skies.add(r)
                    Bucket.TRANSPARENT -> transparents.add(r)
                    Bucket.SCREEN -> screens.add(r)
                }
            }
        }
        val guiRenderers = passDeclaration.guis.map {
            GuiRenderer(inventory, renderContext.width, renderContext.height, it)
        }
        screens.addAll(guiRenderers.flatMap { it.renderables })
        touchBoxes.addAll(guiRenderers.flatMap { it.touchBoxes })
    }

    private fun fillLightUniforms(passDeclaration: PassDeclaration) {
        if (passDeclaration.directionalLights.isEmpty() && passDeclaration.pointLights.isEmpty()) {
            passDeclaration.directionalLights.add(DirectionalLightDeclaration(Vec3(1f, -1f, 1f).normalize(), Color(1f, 7f, 7f, 7f)))
        }
        lightUniforms["ambientColor"] = passDeclaration.ambientLightColor
        lightUniforms["numDirectionalLights"] = passDeclaration.directionalLights.size
        (0 until 32).forEach { i ->
            lightUniforms["directionalLights[$i].dir"] = if (i < passDeclaration.directionalLights.size) passDeclaration.directionalLights[i].direction else Vec3.ZERO
            lightUniforms["directionalLights[$i].color"] = if (i < passDeclaration.directionalLights.size) passDeclaration.directionalLights[i].color else Color.White
        }
        lightUniforms["numPointLights"] = passDeclaration.pointLights.size
        (0 until 32).forEach { i ->
            lightUniforms["pointLights[$i].pos"] = if (i < passDeclaration.pointLights.size) passDeclaration.pointLights[i].position else Vec3.ZERO
            lightUniforms["pointLights[$i].color"] = if (i < passDeclaration.pointLights.size) passDeclaration.pointLights[i].color else Color.White
        }
    }

    fun render(contextUniforms: Map<String, Any?>, fixer: (Any?) -> Any?) {
        val back = renderContext.backgroundColor
        glClearColor(back.r, back.g, back.b, back.a)
        glViewport(0, 0, renderContext.width, renderContext.height)
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
        transparents.sortedByDescending { (renderContext.camera.mat4 * it.transform.offset()).z }
            .forEach { it.render(contextUniforms, fixer) }
        glDepthMask(true)
    }

}