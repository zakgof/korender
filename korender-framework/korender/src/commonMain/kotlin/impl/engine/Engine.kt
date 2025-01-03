package com.zakgof.korender.impl.engine

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.AsyncContext
import com.zakgof.korender.BlurParams
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.IndexType
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.OrthoProjectionDeclaration
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.RenderingOption
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.WaterParams
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.checkGlError
import com.zakgof.korender.impl.geometry.Cube
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.HeightField
import com.zakgof.korender.impl.geometry.ObjMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.geometry.Sphere
import com.zakgof.korender.impl.material.InternalAdjustParams
import com.zakgof.korender.impl.material.InternalBlurParams
import com.zakgof.korender.impl.material.InternalFastCloudSkyParams
import com.zakgof.korender.impl.material.InternalFireParams
import com.zakgof.korender.impl.material.InternalFireballParams
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalSmokeParams
import com.zakgof.korender.impl.material.InternalStandartParams
import com.zakgof.korender.impl.material.InternalWaterParams
import com.zakgof.korender.impl.material.ParamUniforms
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.OrthoProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.channels.Channel

internal class Engine(
    width: Int,
    height: Int,
    asyncContext: AsyncContext,
    block: KorenderContext.() -> Unit
) {

    private val touchQueue = Channel<TouchEvent>(Channel.UNLIMITED)
    private val frameBlocks = mutableListOf<FrameContext.() -> Unit>()
    private val inventory = Inventory(asyncContext)
    private val renderContext = RenderContext(width, height)

    private lateinit var sceneTouchBoxesHandler: (TouchEvent) -> Boolean
    private val touchHandlers = mutableListOf<TouchHandler>()

    inner class KorenderContextImpl : KorenderContext {
        override fun Frame(block: FrameContext.() -> Unit) {
            frameBlocks.add(block)
        }

        override fun OnTouch(handler: (TouchEvent) -> Unit) {
            touchHandlers.add(handler)
        }

        override fun texture(
            textureResource: String,
            filter: TextureFilter,
            wrap: TextureWrap,
            aniso: Int
        ): TextureDeclaration = ResourceTextureDeclaration(textureResource, filter, wrap, aniso)

        override fun cube(halfSide: Float): MeshDeclaration = Cube(halfSide)

        override fun sphere(radius: Float): MeshDeclaration = Sphere(radius)

        override fun obj(objFile: String): MeshDeclaration = ObjMesh(objFile)

        override fun screenQuad(): MeshDeclaration = ScreenQuad

        override fun customMesh(
            id: Any,
            vertexCount: Int,
            indexCount: Int,
            vararg attributes: MeshAttribute,
            dynamic: Boolean,
            indexType: IndexType?,
            block: MeshInitializer.() -> Unit
        ): MeshDeclaration =
            CustomMesh(id, vertexCount, indexCount, attributes.asList(), dynamic, indexType, block)

        override fun heightField(
            id: Any,
            cellsX: Int,
            cellsZ: Int,
            cellWidth: Float,
            height: (Int, Int) -> Float
        ): MeshDeclaration =
            HeightField(id, cellsX, cellsZ, cellWidth, height)

        override fun vertex(vertShaderFile: String): InternalMaterialModifier =
            InternalMaterialModifier { it.vertShaderFile = vertShaderFile }

        override fun fragment(fragShaderFile: String): InternalMaterialModifier =
            InternalMaterialModifier { it.fragShaderFile = fragShaderFile }

        override fun defs(vararg defs: String): InternalMaterialModifier =
            InternalMaterialModifier { it.shaderDefs += setOf(*defs) }

        override fun plugin(name: String, shaderFile: String): InternalMaterialModifier =
            InternalMaterialModifier { it.plugins[name] = shaderFile }

        override fun options(vararg options: RenderingOption): InternalMaterialModifier =
            InternalMaterialModifier { it.options += setOf(*options) }

        override fun standart(vararg options: RenderingOption, block: StandartParams.() -> Unit) =
            InternalMaterialModifier {
                val pu = ParamUniforms(InternalStandartParams(), block)
                it.shaderDefs += pu.shaderDefs()
                it.options += options.asList()
                it.shaderUniforms = pu
            }

        override fun blurVert(block: BlurParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/blurh.frag"
                it.shaderUniforms = ParamUniforms(InternalBlurParams(), block)
            }

        override fun blurHorz(block: BlurParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/blurv.frag"
                it.shaderUniforms = ParamUniforms(InternalBlurParams(), block)
            }

        override fun adjust(block: AdjustParams.() -> Unit): MaterialModifier =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/adjust.frag"
                it.shaderUniforms = ParamUniforms(InternalAdjustParams(), block)
            }

        override fun fire(block: FireParams.() -> Unit) =
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/billboard.vert"
                it.fragShaderFile = "!shader/effect/fire.frag"
                it.shaderUniforms = ParamUniforms(InternalFireParams(), block)
            }

        override fun fireball(block: FireballParams.() -> Unit) =
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/billboard.vert"
                it.fragShaderFile = "!shader/effect/fireball.frag"
                it.shaderUniforms = ParamUniforms(InternalFireballParams(), block)
            }

        override fun smoke(block: SmokeParams.() -> Unit) =
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/billboard.vert"
                it.fragShaderFile = "!shader/effect/smoke.frag"
                it.shaderUniforms = ParamUniforms(InternalSmokeParams(), block)
            }

        override fun water(block: WaterParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/water.frag"
                it.shaderUniforms = ParamUniforms(InternalWaterParams(), block)
            }

        override fun fxaa() =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/fxaa.frag"
            }

        override fun fastCloudSky(block: FastCloudSkyParams.() -> Unit) =
            InternalMaterialModifier {
                it.plugins["sky"] = "!shader/sky/fastcloud.plugin.frag"
                it.pluginUniforms += ParamUniforms(InternalFastCloudSkyParams(), block)
            }

        override fun frustum(width: Float, height: Float, near: Float, far: Float): FrustumProjectionDeclaration =
            FrustumProjection(width, height, near, far)

        override fun ortho(width: Float, height: Float, near: Float, far: Float): OrthoProjectionDeclaration =
            OrthoProjection(width, height, near, far)

        override fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration =
            DefaultCamera(position, direction, up)

        override var camera: CameraDeclaration
            get() = renderContext.camera
            set(value) {
                renderContext.camera = value as Camera
            }

        override var projection: ProjectionDeclaration
            get() = renderContext.projection
            set(value) {
                renderContext.projection = value as Projection
            }

        override var background: Color
            get() = renderContext.backgroundColor
            set(value) {
                renderContext.backgroundColor = value
            }

        override val width: Int
            get() = renderContext.width

        override val height: Int
            get() = renderContext.height
    }

    init {
        println("Engine init $width x $height")
        block.invoke(KorenderContextImpl())
    }

    fun frame() {
        val frameInfo = renderContext.frameInfoManager.frame()
        processTouches()
        val sd = SceneDeclaration()
        frameBlocks.forEach {
            DefaultFrameContext(sd, frameInfo).apply(it)
        }
        inventory.go {
            val scene = Scene(sd, inventory, renderContext, frameInfo.time)
            scene.render()
            checkGlError()
            sceneTouchBoxesHandler = scene.touchBoxesHandler
        }
    }

    suspend fun pushTouch(touchEvent: TouchEvent) = touchQueue.send(touchEvent)

    private fun processTouches() {
        do {
            val event = touchQueue.tryReceive().getOrNull()
            event?.let { touchEvent ->
                if (!sceneTouchBoxesHandler(touchEvent)) {
                    touchHandlers.forEach { it(touchEvent) }
                }
            }
        } while (event != null)
    }

    fun resize(w: Int, h: Int) {
        println("Engine resize $w x $h")
        renderContext.width = w
        renderContext.height = h
    }
}