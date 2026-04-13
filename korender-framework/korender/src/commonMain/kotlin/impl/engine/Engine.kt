package com.zakgof.korender.impl.engine

import com.zakgof.korender.BaseMaterialScope
import com.zakgof.korender.BillboardMaterialScope
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialScope
import com.zakgof.korender.MutableMesh
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.Platform
import com.zakgof.korender.PostProcessMaterialScope
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShaderPlugin
import com.zakgof.korender.ShaderPluginId
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.context.FrameScope
import com.zakgof.korender.context.BillboardInstancingScope
import com.zakgof.korender.context.GltfInstancingScope
import com.zakgof.korender.context.InstancingDeclaration
import com.zakgof.korender.context.InstancingParameter
import com.zakgof.korender.context.InstancingScope
import com.zakgof.korender.context.KorenderScope
import com.zakgof.korender.context.ResourceScope
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.context.DefaultFrameScope
import com.zakgof.korender.impl.context.DefaultBillboardInstancingScope
import com.zakgof.korender.impl.context.DefaultGltfInstancingScope
import com.zakgof.korender.impl.context.DefaultInstancingScope
import com.zakgof.korender.impl.context.NodeContext
import com.zakgof.korender.impl.engine.shadow.InternalHardShadow
import com.zakgof.korender.impl.engine.shadow.InternalHardwarePcfShadow
import com.zakgof.korender.impl.engine.shadow.InternalSoftwarePcfShadow
import com.zakgof.korender.impl.engine.shadow.InternalVsmShadow
import com.zakgof.korender.impl.geometry.InternalMutableMesh
import com.zakgof.korender.impl.geometry.MeshAttributes
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_SEAMLESS
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.impl.image.InternalImage3D
import com.zakgof.korender.impl.load
import com.zakgof.korender.impl.material.AdjustmentMaterial
import com.zakgof.korender.impl.material.BlurMaterial
import com.zakgof.korender.impl.material.CubeSkyMaterial
import com.zakgof.korender.impl.material.FastCloudSkyMaterial
import com.zakgof.korender.impl.material.FireEffect
import com.zakgof.korender.impl.material.FireballEffect
import com.zakgof.korender.impl.material.FogMaterial
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.material.InternalBillboardMaterial
import com.zakgof.korender.impl.material.InternalDecalMaterial
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalPipeMaterial
import com.zakgof.korender.impl.material.InternalPostProcessingMaterial
import com.zakgof.korender.impl.material.InternalSkyMaterial
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ProbeTextureDeclaration
import com.zakgof.korender.impl.material.SmokeEffect
import com.zakgof.korender.impl.material.StarrySkyMaterial
import com.zakgof.korender.impl.material.TextureSkyMaterial
import com.zakgof.korender.impl.material.WaterMaterial
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.LogProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel

internal class Engine(
    width: Int,
    height: Int,
    kmpResourceLoader: ResourceLoader,
    block: KorenderScope.() -> Unit,
) {

    private val touchQueue = Channel<TouchEvent>(Channel.UNLIMITED)
    private val keyQueue = Channel<KeyEvent>(Channel.UNLIMITED)
    private val frameBlocks = mutableListOf<FrameScope.() -> Unit>()
    private val loader = Loader()
    private val inventory = Inventory(loader)
    private val renderContext = RenderContext()
    private val regularFrameContext = RegularFrameContext(width, height, renderContext)

    private var touchBoxes: List<TouchBox> = listOf()
    private var pressedTouchBoxIds = setOf<Any>()
    private val touchHandlers = mutableListOf<TouchHandler>()
    private val keyHandlers = mutableListOf<KeyHandler>()
    private var loaderLoaded = false
    private var loaderComplete = false
    private val preFrames = ArrayDeque<() -> Unit>()
    private val renderer = Renderer(inventory, renderContext)
    private val rootNodeContext = NodeContext(kmpResourceLoader, Transform.IDENTITY, TimeRetentionPolicy(10f))
    private val kc = KorenderScopeImpl()

    inner class KorenderScopeImpl : KorenderScope, ResourceScope by rootNodeContext {

        val regularFrameContext = this@Engine.regularFrameContext
        val renderContext = this@Engine.renderContext

        var currentRetentionGeneration: Int = 0
        override val target: KorenderScope.TargetPlatform = Platform.target

        override fun Frame(block: FrameScope.() -> Unit) {
            if (frameBlocks.isNotEmpty())
                throw KorenderException("Only one Frame declaration is allowed")
            frameBlocks.add(block)
        }

        override fun shaderPlugin(id: ShaderPluginId, file: String): ShaderPlugin =
            inventory.shaderPlugin(id, file)

        override fun OnTouch(handler: (TouchEvent) -> Unit) {
            touchHandlers.add(handler)
        }

        override fun OnKey(handler: (KeyEvent) -> Unit) {
            keyHandlers.add(handler)
        }

        override fun textureProbe(frameProbeName: String): TextureDeclaration = ProbeTextureDeclaration(frameProbeName)

        override fun cubeTextureProbe(envProbeName: String): CubeTextureDeclaration = ProbeCubeTextureDeclaration(envProbeName)

        override fun captureEnv(resolution: Int, block: FrameScope.() -> Unit): Deferred<CubeTextureImages> {
            val sd = SceneDeclaration()
            val rfc = RegularFrameContext(resolution, resolution, renderContext)
            block.invoke(DefaultFrameScope(kc, rfc, sd, FrameInfo(), rootNodeContext))
            val images = CompletableDeferred<CubeTextureImages>()
            val startNano = Platform.nanoTime()

            fun tryRender(): Boolean {
                val rk = ResultKeeper()
                renderer.renderToEnvProbe(CaptureContext(rfc, sd, rootNodeContext), "#immediate", rk)
                    ?.fetch()
                    ?.let {
                        if (rk.success) {
                            images.complete(it)
                            println("Capture env done in ${(Platform.nanoTime() - startNano) * 1e-9}s")
                            return true
                        }
                    }
                println("Capturing env not complete, retrying...")
                return false
            }

            fun cycle() {
                preFrames.addLast {
                    if (!tryRender()) {
                        cycle()
                    }
                }
            }
            if (!tryRender()) {
                cycle()
            }
            return images
        }

        override fun captureFrame(width: Int, height: Int, block: FrameScope.() -> Unit): Deferred<Image> {
            val sd = SceneDeclaration()
            val rfc = RegularFrameContext(width, height, renderContext)
            block.invoke(DefaultFrameScope(kc, rfc, sd, FrameInfo(), rootNodeContext))
            val image = CompletableDeferred<Image>()
            val startNano = Platform.nanoTime()

            fun tryRender(): Boolean {
                val rk = ResultKeeper()
                renderer.renderToFrameProbe(CaptureContext(rfc, sd, rootNodeContext), "#immediate", rk)
                    ?.fetch()
                    ?.let {
                        if (rk.success) {
                            image.complete(it)
                            println("Capture frame done in ${(Platform.nanoTime() - startNano) * 1e-9}s")
                            return true
                        }
                    }
                println("Capturing frame not complete, retrying...")
                return false
            }

            fun cycle() {
                preFrames.addLast {
                    if (!tryRender()) {
                        cycle()
                    }
                }
            }
            if (!tryRender()) {
                cycle()
            }
            return image
        }

        override fun mutableMesh(): MutableMesh =
            InternalMutableMesh()

        override fun customMaterial(vertShaderFile: String, fragShaderFile: String, block: MaterialScope.() -> Unit) =
            InternalMaterial(vertShaderFile, fragShaderFile).also { block.invoke(it) }

        override fun customMaterial(vertShaderFile: String, block: BaseMaterialScope.() -> Unit) =
            InternalBaseMaterial(vertShaderFile).also { block.invoke(it) }

        override fun base(block: BaseMaterialScope.() -> Unit) =
            InternalBaseMaterial().also { block.invoke(it) }

        override fun billboard(block: BillboardMaterialScope.() -> Unit) =
            InternalBillboardMaterial().also { block.invoke(it) }

        override fun pipe(block: BaseMaterialScope.() -> Unit) =
            InternalPipeMaterial().also { block.invoke(it) }

        override fun decal(block: BaseMaterialScope.() -> Unit) =
            InternalDecalMaterial().also { block.invoke(it) }

        override fun blurVert(radius: Float) =
            BlurMaterial(true, radius)

        override fun blurHorz(radius: Float) =
            BlurMaterial(false, radius)

        override fun adjust(brightness: Float, contrast: Float, saturation: Float) =
            AdjustmentMaterial(brightness, contrast, saturation)

        override fun fire(strength: Float) =
            FireEffect(strength)

        override fun fireball(power: Float) =
            FireballEffect(power)

        override fun smoke(density: Float, seed: Float) =
            SmokeEffect(density, seed)

        override fun water(waterColor: ColorRGB, transparency: Float, waveScale: Float, waveMagnitude: Float, sky: SkyMaterial) =
            WaterMaterial(waterColor, transparency, waveScale, waveMagnitude, sky as InternalSkyMaterial)

        override fun fxaa() =
            InternalPostProcessingMaterial("!shader/effect/fxaa.frag")

        override fun customPostProcessingFilter(fragmentShaderFile: String, block: PostProcessMaterialScope.() -> Unit) =
            InternalPostProcessingMaterial(fragmentShaderFile).also { block.invoke(it) }

        override fun fastCloudSky(density: Float, thickness: Float, scale: Float, rippleAmount: Float, rippleScale: Float, zenithColor: ColorRGB, horizonColor: ColorRGB, cloudLight: Float, cloudDark: Float, block: MaterialScope.() -> Unit) =
            FastCloudSkyMaterial(density, thickness, scale, rippleAmount, rippleScale, zenithColor, horizonColor, cloudLight, cloudDark, block)

        override fun starrySky(colorness: Float, density: Float, speed: Float, size: Float, block: MaterialScope.() -> Unit) =
            StarrySkyMaterial(colorness, density, speed, size, block)

        override fun cubeSky(cubeTexture: CubeTextureDeclaration, block: MaterialScope.() -> Unit) =
            CubeSkyMaterial(cubeTexture, block)

        override fun textureSky(texture: TextureDeclaration, block: MaterialScope.() -> Unit) =
            TextureSkyMaterial(texture, block)

        override fun fog(density: Float, color: ColorRGB) =
            FogMaterial(density, color)

        override fun projection(width: Float, height: Float, near: Float, far: Float, mode: ProjectionMode) =
            Projection(width, height, near, far, mode)

        override fun frustum() = FrustumProjectionMode

        override fun ortho() = OrthoProjectionMode

        override fun log(c: Float) = LogProjectionMode(c)

        override fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration =
            DefaultCamera(position, direction.normalize(), up.normalize())

        override
        var retentionPolicy: RetentionPolicy
            get() = rootNodeContext.retentionPolicy
            set(value) {
                rootNodeContext.retentionPolicy = value
            }

        override
        var retentionGeneration: Int
            get() = currentRetentionGeneration
            set(value) {
                currentRetentionGeneration = value
            }

        override
        var camera: CameraDeclaration
            get() = regularFrameContext.camera
            set(value) {
                regularFrameContext.camera = value as Camera
            }

        override
        var projection: ProjectionDeclaration
            get() = regularFrameContext.projection
            set(value) {
                regularFrameContext.projection = value as Projection
            }

        override
        var background: ColorRGBA
            get() = renderContext.backgroundColor
            set(value) {
                renderContext.backgroundColor = value
            }

        override
        val width: Int
            get() = regularFrameContext.width

        override
        val height: Int
            get() = regularFrameContext.height

        override fun createImage(width: Int, height: Int, format: PixelFormat): Image =
            Platform.createImage(width, height, format)

        override fun createImage3D(width: Int, height: Int, depth: Int, format: PixelFormat): Image3D =
            InternalImage3D(width, height, depth, NativeByteBuffer(width * height * depth * format.bytes), format)

        override fun loadImage(imageResource: String): Deferred<Image> = CoroutineScope(Dispatchers.Default).async {
            val bytes = rootNodeContext.resourceLoader.load(imageResource)
            Platform.loadImage(bytes, imageResource.split(".").last()).await()
        }

        override fun loadImage(bytes: ByteArray, type: String): Deferred<Image> =
            Platform.loadImage(bytes, type)

        override fun vsm(blurRadius: Float?): ShadowAlgorithmDeclaration =
            InternalVsmShadow(blurRadius)

        override fun hard(): ShadowAlgorithmDeclaration =
            InternalHardShadow()

        override fun softwarePcf(samples: Int, blurRadius: Float, bias: Float): ShadowAlgorithmDeclaration =
            InternalSoftwarePcfShadow(samples, blurRadius, bias)

        override fun hardwarePcf(bias: Float): ShadowAlgorithmDeclaration =
            InternalHardwarePcfShadow(bias)

        override fun instancing(id: String, count: Int, dynamic: Boolean, vararg parameter: InstancingParameter, block: InstancingScope.() -> Unit): InstancingDeclaration {
            InternalInstancingDeclaration(id, count, dynamic, parameter.toSet()) {
                val instances = mutableListOf<MeshInstance>()
                val context = DefaultInstancingScope(instances)
                block.invoke(context)
                instances
            }

        override fun billboardInstancing(id: String, count: Int, dynamic: Boolean, block: BillboardInstancingScope.() -> Unit) =
            InternalBillboardInstancingDeclaration(id, count, dynamic) {
                val instances = mutableListOf<BillboardInstance>()
                val context = DefaultBillboardInstancingScope(instances)
                block.invoke(context)
                instances
            }

        override fun gltfInstancing(id: String, count: Int, dynamic: Boolean, block: GltfInstancingScope.() -> Unit) =
            InternalGltfInstancingDeclaration(id, count, dynamic) {
                val instances = mutableListOf<GltfInstance>()
                val context = DefaultGltfInstancingScope(instances)
                block.invoke(context)
                instances
            }

        override fun immediatelyFree() = ImmediatelyFreeRetentionPolicy
        override fun keepForever() = KeepForeverRetentionPolicy
        override fun untilGeneration(generation: Int) = UntilGenerationRetentionPolicy(generation)
        override fun time(seconds: Float) = TimeRetentionPolicy(seconds)

        override
        val POS = MeshAttributes.POS

        override
        val NORMAL = MeshAttributes.NORMAL

        override
        val TEX = MeshAttributes.TEX

        override
        val JOINTS_BYTE = MeshAttributes.JOINTS_BYTE

        override
        val JOINTS_SHORT = MeshAttributes.JOINTS_SHORT

        override
        val JOINTS_INT = MeshAttributes.JOINTS_INT

        override
        val WEIGHTS = MeshAttributes.WEIGHTS

        override
        val SCALE = MeshAttributes.SCALE

        override
        val INSTCOLORTEXINDEX = MeshAttributes.INSTCOLORTEXINDEX

        override
        val B1 = MeshAttributes.B1

        override
        val B2 = MeshAttributes.B2

        override
        val B3 = MeshAttributes.B3

        override
        val MODEL0 = MeshAttributes.MODEL0

        override
        val MODEL1 = MeshAttributes.MODEL1

        override
        val MODEL2 = MeshAttributes.MODEL2

        override
        val MODEL3 = MeshAttributes.MODEL3

        override
        val INSTPOS = MeshAttributes.INSTPOS

        override
        val INSTSCALE =
            MeshAttributes.INSTSCALE

        override
        val INSTROT =
            MeshAttributes.INSTROT

        override
        val INSTTEX =
            MeshAttributes.INSTTEX

        override
        val INSTSCREEN =
            MeshAttributes.INSTSCREEN
    }

    init {
        println("Engine init $width x $height")
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
            DefaultFrameScope(kc, kc.regularFrameContext, sd, frameInfo, rootNodeContext).apply(it)
        }
        inventory.go(frameInfo.time, kc.currentRetentionGeneration) {
            preFrames.removeFirstOrNull()?.let { it() }

            val loaderScene = sd.loaderSceneDeclaration?.let { renderer.Scene(rootNodeContext, it, regularFrameContext) }

            if (loaderScene != null && !loaderLoaded) {
                loaderLoaded = ResultKeeper().also(loaderScene::render).success
                false
            } else {
                val scene = renderer.Scene(rootNodeContext, sd, regularFrameContext)
                val success = ResultKeeper().also(scene::render).success
                if (loaderScene != null && (!loaderComplete || inventory.pending() > 0)) {
                    loaderScene.render(null)
                }
                touchBoxes = scene.touchBoxes
                if (success) loaderComplete = true
                success && preFrames.isEmpty()
            }
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
        regularFrameContext.width = w
        regularFrameContext.height = h
    }
}



