package com.zakgof.korender.impl.engine

import com.zakgof.korender.camera.Camera
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.impl.gl.VGL11
import com.zakgof.korender.math.Vec3

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
        VGL11.glEnable(VGL11.GL_BLEND)
        VGL11.glEnable(VGL11.GL_DEPTH_TEST)
        VGL11.glBlendFunc(VGL11.GL_SRC_ALPHA, VGL11.GL_ONE_MINUS_SRC_ALPHA)
        VGL11.glDepthFunc(VGL11.GL_LEQUAL)
        VGL11.glClearColor(0f, 0f, 0f, 0f)
        VGL11.glViewport(0, 0, width, height)
        VGL11.glEnable(VGL11.GL_CULL_FACE)
        VGL11.glCullFace(VGL11.GL_BACK)
        VGL11.glClear(VGL11.GL_COLOR_BUFFER_BIT or VGL11.GL_DEPTH_BUFFER_BIT)
        opaques.forEach { it.render(uniformDecorator) }
        skies.forEach { it.render(uniformDecorator) }
        VGL11.glDepthMask(false)
        transparents.sortedByDescending { (camera.mat4 * (it.transform.mat4() * Vec3.ZERO)).z }
            .forEach { it.render(uniformDecorator) }
        VGL11.glDepthMask(true)
        screens.forEach { it.render(uniformDecorator) }
    }
}