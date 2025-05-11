package com.zakgof.korender.impl.engine

import com.zakgof.korender.AdjustParams
import com.zakgof.korender.AsyncContext
import com.zakgof.korender.BaseParams
import com.zakgof.korender.BloomParams
import com.zakgof.korender.BlurParams
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.FastCloudSkyParams
import com.zakgof.korender.FireParams
import com.zakgof.korender.FireballParams
import com.zakgof.korender.FogParams
import com.zakgof.korender.FrustumProjectionDeclaration
import com.zakgof.korender.GrassParams
import com.zakgof.korender.Image
import com.zakgof.korender.IndexType
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.OrthoProjectionDeclaration
import com.zakgof.korender.Platform
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.SmokeParams
import com.zakgof.korender.SsrParams
import com.zakgof.korender.StandartParams
import com.zakgof.korender.StarrySkyParams
import com.zakgof.korender.TerrainParams
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.WaterParams
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.InstancingDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.context.RoiTexturesContext
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.context.DefaultFrameContext
import com.zakgof.korender.impl.engine.shadow.InternalHardParams
import com.zakgof.korender.impl.engine.shadow.InternalPcssParams
import com.zakgof.korender.impl.engine.shadow.InternalVsmParams
import com.zakgof.korender.impl.geometry.Cube
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.HeightField
import com.zakgof.korender.impl.geometry.ObjMesh
import com.zakgof.korender.impl.geometry.ScreenQuad
import com.zakgof.korender.impl.geometry.Sphere
import com.zakgof.korender.impl.gl.GL.glBlendFunc
import com.zakgof.korender.impl.gl.GL.glCullFace
import com.zakgof.korender.impl.gl.GL.glDepthFunc
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_BACK
import com.zakgof.korender.impl.gl.GLConstants.GL_BLEND
import com.zakgof.korender.impl.gl.GLConstants.GL_CULL_FACE
import com.zakgof.korender.impl.gl.GLConstants.GL_DEPTH_TEST
import com.zakgof.korender.impl.gl.GLConstants.GL_LEQUAL
import com.zakgof.korender.impl.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import com.zakgof.korender.impl.gl.GLConstants.GL_SRC_ALPHA
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_SEAMLESS
import com.zakgof.korender.impl.glgpu.GlGpuCubeTexture
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.InternalAdjustParams
import com.zakgof.korender.impl.material.InternalBaseParams
import com.zakgof.korender.impl.material.InternalBloomParams
import com.zakgof.korender.impl.material.InternalBlurParams
import com.zakgof.korender.impl.material.InternalFastCloudSkyParams
import com.zakgof.korender.impl.material.InternalFireParams
import com.zakgof.korender.impl.material.InternalFireballParams
import com.zakgof.korender.impl.material.InternalFogParams
import com.zakgof.korender.impl.material.InternalGrassParams
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostShadingEffect
import com.zakgof.korender.impl.material.InternalRoiTexturesContext
import com.zakgof.korender.impl.material.InternalSmokeParams
import com.zakgof.korender.impl.material.InternalSsrParams
import com.zakgof.korender.impl.material.InternalStandartParams
import com.zakgof.korender.impl.material.InternalStarrySkyParams
import com.zakgof.korender.impl.material.InternalTerrainParams
import com.zakgof.korender.impl.material.InternalWaterParams
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.prefab.grass.Grass
import com.zakgof.korender.impl.prefab.terrain.Clipmaps
import com.zakgof.korender.impl.projection.FrustumProjection
import com.zakgof.korender.impl.projection.OrthoProjection
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel

internal class Engine(
    width: Int,
    height: Int,
    private val asyncContext: AsyncContext,
    block: KorenderContext.() -> Unit
) {

    private val touchQueue = Channel<TouchEvent>(Channel.UNLIMITED)
    private val keyQueue = Channel<KeyEvent>(Channel.UNLIMITED)
    private val frameBlocks = mutableListOf<FrameContext.() -> Unit>()
    private val inventory = Inventory(asyncContext)
    private val renderContext = RenderContext(width, height)
    private val probes = mutableMapOf<String, GlGpuCubeTexture>()

    private var touchBoxes: List<TouchBox> = listOf()
    private var pressedTouchBoxIds = setOf<Any>()
    private val touchHandlers = mutableListOf<TouchHandler>()
    private val keyHandlers = mutableListOf<KeyHandler>()
    private val kc = KorenderContextImpl()

    inner class KorenderContextImpl : KorenderContext {

        override val target: KorenderContext.TargetPlatform = Platform.target

        override fun Frame(block: FrameContext.() -> Unit) {
            if (frameBlocks.isNotEmpty())
                throw KorenderException("Only one Frame declaration is allowed")
            frameBlocks.add(block)
        }

        override fun OnTouch(handler: (TouchEvent) -> Unit) {
            touchHandlers.add(handler)
        }

        override fun OnKey(handler: (KeyEvent) -> Unit) {
            keyHandlers.add(handler)
        }

        override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureDeclaration =
            ResourceTextureDeclaration(textureResource, filter, wrap, aniso)

        override fun cubeTexture(nxResource: String, nyResource: String, nzResource: String, pxResource: String, pyResource: String, pzResource: String): CubeTextureDeclaration =
            ResourceCubeTextureDeclaration(nxResource, nyResource, nzResource, pxResource, pyResource, pzResource)

        override fun cubeTexture(id: String, nxImage: Image, nyImage: Image, nzImage: Image, pxImage: Image, pyImage: Image, pzImage: Image): CubeTextureDeclaration =
            ImageCubeTextureDeclaration(id, nxImage, nyImage, nzImage, pxImage, pyImage, pzImage)

        override fun cubeProbe(probeName: String): CubeTextureDeclaration = ProbeCubeTextureDeclaration(probeName)

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

        override fun plugin(name: String, shaderFile: String) = InternalMaterialModifier {
            it.plugins[name] = shaderFile
            it.shaderDefs += "PLUGIN_" + name.uppercase()
        }

        override fun standart(block: StandartParams.() -> Unit) = InternalMaterialModifier {
            InternalStandartParams().apply(block).collect(it)
        }

        override fun uniforms(block: BaseParams.() -> Unit): MaterialModifier = InternalMaterialModifier {
            InternalBaseParams().apply(block).collect(it)
        }

        override fun terrain(block: TerrainParams.() -> Unit): MaterialModifier = InternalMaterialModifier {
            InternalTerrainParams().apply(block).collect(it)
        }

        override fun blurVert(block: BlurParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/blurh.frag"
                InternalBlurParams().apply(block).collect(it)
            }

        override fun blurHorz(block: BlurParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/blurv.frag"
                InternalBlurParams().apply(block).collect(it)
            }

        override fun adjust(block: AdjustParams.() -> Unit): MaterialModifier =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/adjust.frag"
                InternalAdjustParams().apply(block).collect(it)
            }

        override fun fire(block: FireParams.() -> Unit) =
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/billboard.vert"
                it.fragShaderFile = "!shader/effect/fire.frag"
                InternalFireParams().apply(block).collect(it)
            }

        override fun fireball(block: FireballParams.() -> Unit) =
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/billboard.vert"
                it.fragShaderFile = "!shader/effect/fireball.frag"
                InternalFireballParams().apply(block).collect(it)
            }

        override fun smoke(block: SmokeParams.() -> Unit) =
            InternalMaterialModifier {
                it.vertShaderFile = "!shader/billboard.vert"
                it.fragShaderFile = "!shader/effect/smoke.frag"
                InternalSmokeParams().apply(block).collect(it)
            }

        override fun grass(block: GrassParams.() -> Unit) =
            InternalMaterialModifier {
                InternalGrassParams().apply(block).collect(it)
            }

        override fun water(block: WaterParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/water.frag"
                InternalWaterParams().apply(block).collect(it)
            }

        override fun fxaa() =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/fxaa.frag"
            }

        override fun fastCloudSky(block: FastCloudSkyParams.() -> Unit) =
            InternalMaterialModifier {
                it.plugins["sky"] = "!shader/sky/fastcloud.plugin.frag"
                InternalFastCloudSkyParams().apply(block).collect(it)
            }

        override fun starrySky(block: StarrySkyParams.() -> Unit) =
            InternalMaterialModifier {
                it.plugins["sky"] = "!shader/sky/starry.plugin.frag"
                InternalStarrySkyParams().apply(block).collect(it)
            }

        override fun cubeSky(cubeTexture: CubeTextureDeclaration) =
            InternalMaterialModifier {
                it.plugins["sky"] = "!shader/sky/cube.plugin.frag"
                it.shaderDefs += "SKY_CUBE"
                it.uniforms["cubeTexture"] = cubeTexture
            }

        override fun fog(block: FogParams.() -> Unit) =
            InternalMaterialModifier {
                it.fragShaderFile = "!shader/effect/fog.frag"
                InternalFogParams().apply(block).collect(it)
            }

        override fun ibl(env: CubeTextureDeclaration): MaterialModifier =
            InternalMaterialModifier {
                it.shaderDefs += "IBL"
                it.uniforms["cubeTexture"] = env
            }

        override fun roiTextures(block: RoiTexturesContext.() -> Unit): MaterialModifier =
            InternalMaterialModifier {
                it.shaderDefs += "ROI"
                InternalRoiTexturesContext().apply(block).collect(it)
            }

        override fun ssr(width: Int?, height: Int?, fxaa: Boolean, block: SsrParams.() -> Unit): PostShadingEffect {
            val w = width ?: renderContext.width
            val h = height ?: renderContext.height
            return InternalPostShadingEffect("ssr", w, h,
                effectPassMaterialModifiers =
                listOf(
                    InternalMaterialModifier {
                        it.fragShaderFile = "!shader/effect/ssr.frag"
                        InternalSsrParams().apply(block).collect(it)
                    }
                ),
                "ssrTexture",
                "ssrDepthTexture",
                compositionMaterialModifier = {
                    it.shaderDefs += "SSR"
                    if (fxaa) {
                        it.shaderDefs += "SSR_FXAA"
                        it.uniforms["ssrWidth"] = w.toFloat()
                        it.uniforms["ssrHeight"] = h.toFloat()
                    }
                })
        }

        override fun bloom(width: Int?, height: Int?, block: BloomParams.() -> Unit): PostShadingEffect = InternalPostShadingEffect(
            "bloom",
            width ?: renderContext.width,
            height ?: renderContext.height,
            effectPassMaterialModifiers = listOf(
                InternalMaterialModifier {
                    it.fragShaderFile = "!shader/effect/bloom.frag"
                    InternalBloomParams().apply(block).collect(it)
                },
                InternalMaterialModifier {
                    it.fragShaderFile = "!shader/effect/blurv.frag"
                    it.uniforms["screenHeight"] = (width ?: renderContext.height).toFloat()
                    it.uniforms["radius"] = 2.2f
                },
                InternalMaterialModifier {
                    it.fragShaderFile = "!shader/effect/blurh.frag"
                    it.uniforms["screenWidth"] = (height ?: renderContext.width).toFloat()
                    it.uniforms["radius"] = 2.2f
                }
            ),
            "bloomTexture",
            "bloomDepthTexture",
            compositionMaterialModifier = {
                it.shaderDefs += "BLOOM"
            })

        override fun frustum(width: Float, height: Float, near: Float, far: Float): FrustumProjectionDeclaration =
            FrustumProjection(width, height, near, far)

        override fun ortho(width: Float, height: Float, near: Float, far: Float): OrthoProjectionDeclaration =
            OrthoProjection(width, height, near, far)

        override fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration =
            DefaultCamera(position, direction.normalize(), up.normalize())

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

        override var background: ColorRGB
            get() = renderContext.backgroundColor
            set(value) {
                renderContext.backgroundColor = value
            }

        override val width: Int
            get() = renderContext.width

        override val height: Int
            get() = renderContext.height

        override fun createImage(width: Int, height: Int, format: Image.Format): Image =
            Platform.createImage(width, height, format)

        override fun loadImage(imageResource: String): Deferred<Image> {
            return asyncContext.call {
                val bytes = resourceBytes(asyncContext.appResourceLoader, imageResource)
                Platform.loadImage(bytes, imageResource.split(".").last()).await()
            }
        }

        override fun vsm(blurRadius: Float?): ShadowAlgorithmDeclaration =
            InternalVsmParams(blurRadius)

        override fun hard(): ShadowAlgorithmDeclaration =
            InternalHardParams()

        override fun pcss(samples: Int, blurRadius: Float): ShadowAlgorithmDeclaration =
            InternalPcssParams(samples, blurRadius)

        override fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab =
            Clipmaps(this, id, cellSize, hg, rings)

        override fun grassPrefab(id: String, segments: Int, cell: Float, side: Int, filter: (Vec3) -> Boolean): Prefab =
            Grass(this, id, segments, cell, side, filter)

        override fun positionInstancing(id: String, instanceCount: Int, dynamic: Boolean, block: InstancedRenderablesContext.() -> Unit): InstancingDeclaration =
            InternalInstancingDeclaration(id, instanceCount, dynamic, block)
    }

    init {
        println("Engine init $width x $height")
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glDepthFunc(GL_LEQUAL)
        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        // glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        ignoringGlError {
            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS)
        }
        block.invoke(kc)
    }

    fun frame() {
        val frameInfo = renderContext.frameInfoManager.frame()
        processTouches()
        processKeys()
        val sd = SceneDeclaration()
        frameBlocks.forEach {
            DefaultFrameContext(kc, sd, frameInfo).apply(it)
        }
        inventory.go {
            val scene = Scene(sd, inventory, renderContext, probes)
            scene.render()
            // checkGlError("during rendering")
            touchBoxes = scene.touchBoxes
        }
    }

    suspend fun pushTouch(touchEvent: TouchEvent) = touchQueue.send(touchEvent)
    suspend fun pushKey(keyEvent: KeyEvent) = keyQueue.send(keyEvent)

    private fun processTouches() {
        do {
            val event = touchQueue.tryReceive().getOrNull()
            event?.let { touchEvent ->

                val handled = when (touchEvent.type) {

                    TouchEvent.Type.DOWN -> {
                        val hitIds = touchBoxes.filter { it.touch(touchEvent, false) }.map { it.id }
                        pressedTouchBoxIds = hitIds.filterNotNull().toSet()
                        hitIds.isNotEmpty()
                    }

                    TouchEvent.Type.MOVE -> {
                        if (pressedTouchBoxIds.isEmpty()) {
                            false
                            // touchBoxes.fold(false) { acc, tb -> acc or tb.touch(touchEvent, false) }
                        } else {
                            touchBoxes.filter { pressedTouchBoxIds.contains(it.id) }
                                .fold(false) { acc, tb -> acc or tb.touch(touchEvent, true) }
                        }
                    }

                    TouchEvent.Type.UP -> {
                        val handled = if (pressedTouchBoxIds.isEmpty()) {
                            false
                            // touchBoxes.fold(false) { acc, tb -> acc or tb.touch(touchEvent, false) }
                        } else {
                            touchBoxes.filter { pressedTouchBoxIds.contains(it.id) }
                                .fold(false) { acc, tb -> acc or tb.touch(touchEvent, true) }
                        }
                        pressedTouchBoxIds = setOf()
                        handled
                    }
                }
                if (!handled) {
                    touchHandlers.forEach { it(touchEvent) }
                }
            }
        } while (event != null)
    }

    private fun processKeys() {
        do {
            val event = keyQueue.tryReceive().getOrNull()
            event?.let { keyEvent -> keyHandlers.forEach { it(keyEvent) } }
        } while (event != null)
    }

    fun resize(w: Int, h: Int) {
        println("Viewport resize $w x $h")
        renderContext.width = w
        renderContext.height = h
    }
}