package com.zakgof.korender.impl.engine

import com.zakgof.korender.BaseMaterialContext
import com.zakgof.korender.BillboardMaterialContext
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.IndexType
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialContext
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.MutableMesh
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.Platform
import com.zakgof.korender.PostProcessMaterialContext
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.ResourceLoader
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.TerrainMaterial
import com.zakgof.korender.TerrainMaterialContext
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureArrayDeclaration
import com.zakgof.korender.TextureArrayImages
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.context.InstancedBillboardsContext
import com.zakgof.korender.context.InstancedGltfContext
import com.zakgof.korender.context.InstancedRenderablesContext
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.context.PipeMeshContext
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.camera.Camera
import com.zakgof.korender.impl.camera.DefaultCamera
import com.zakgof.korender.impl.context.DefaultFrameContext
import com.zakgof.korender.impl.context.DefaultInstancedBillboardsContext
import com.zakgof.korender.impl.context.DefaultInstancedGltfContext
import com.zakgof.korender.impl.context.DefaultInstancedRenderablesContext
import com.zakgof.korender.impl.engine.shadow.InternalHardShadow
import com.zakgof.korender.impl.engine.shadow.InternalHardwarePcfShadow
import com.zakgof.korender.impl.engine.shadow.InternalSoftwarePcfShadow
import com.zakgof.korender.impl.engine.shadow.InternalVsmShadow
import com.zakgof.korender.impl.geometry.BiQuad
import com.zakgof.korender.impl.geometry.ConeTop
import com.zakgof.korender.impl.geometry.Cube
import com.zakgof.korender.impl.geometry.CustomCpuMesh
import com.zakgof.korender.impl.geometry.CustomMesh
import com.zakgof.korender.impl.geometry.CylinderSide
import com.zakgof.korender.impl.geometry.Disk
import com.zakgof.korender.impl.geometry.Geometry
import com.zakgof.korender.impl.geometry.HeightField
import com.zakgof.korender.impl.geometry.InternalMutableMesh
import com.zakgof.korender.impl.geometry.MeshAttributes
import com.zakgof.korender.impl.geometry.ObjMesh
import com.zakgof.korender.impl.geometry.Quad
import com.zakgof.korender.impl.geometry.Sphere
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_SEAMLESS
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.image.impl.image.InternalImage3D
import com.zakgof.korender.impl.material.AdjustmentMaterial
import com.zakgof.korender.impl.material.BlurMaterial
import com.zakgof.korender.impl.material.CubeSkyMaterial
import com.zakgof.korender.impl.material.FastCloudSkyMaterial
import com.zakgof.korender.impl.material.FireEffect
import com.zakgof.korender.impl.material.FireballEffect
import com.zakgof.korender.impl.material.FogMaterial
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTexture3DDeclaration
import com.zakgof.korender.impl.material.ImageTextureArrayDeclaration
import com.zakgof.korender.impl.material.ImageTextureDeclaration
import com.zakgof.korender.impl.material.InternalBaseMaterial
import com.zakgof.korender.impl.material.InternalBillboardMaterial
import com.zakgof.korender.impl.material.InternalDecalMaterial
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalPipeMaterial
import com.zakgof.korender.impl.material.InternalPostProcessingMaterial
import com.zakgof.korender.impl.material.InternalSkyMaterial
import com.zakgof.korender.impl.material.InternalTerrainMaterial
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ProbeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceTextureArrayDeclaration
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.material.SmokeEffect
import com.zakgof.korender.impl.material.StarrySkyMaterial
import com.zakgof.korender.impl.material.TextureSkyMaterial
import com.zakgof.korender.impl.material.WaterMaterial
import com.zakgof.korender.impl.material.bloomMipEffect
import com.zakgof.korender.impl.material.bloomSimpleEffect
import com.zakgof.korender.impl.material.simpleBlur
import com.zakgof.korender.impl.material.ssrEffect
import com.zakgof.korender.impl.prefab.terrain.Clipmaps
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.LogProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
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
    block: KorenderContext.() -> Unit,
) {

    private val appResourceLoader: ResourceLoader = { resourceBytes(kmpResourceLoader, it) }
    private val touchQueue = Channel<TouchEvent>(Channel.UNLIMITED)
    private val keyQueue = Channel<KeyEvent>(Channel.UNLIMITED)
    private val frameBlocks = mutableListOf<FrameContext.() -> Unit>()
    private val loader = Loader(appResourceLoader)
    private val inventory = Inventory(loader)
    private val renderContext = RenderContext(width, height)

    private var touchBoxes: List<TouchBox> = listOf()
    private var pressedTouchBoxIds = setOf<Any>()
    private val touchHandlers = mutableListOf<TouchHandler>()
    private val keyHandlers = mutableListOf<KeyHandler>()
    private val kc = KorenderContextImpl()
    private var loaderLoaded = false
    private val preFrames = ArrayDeque<() -> Unit>()

    inner class KorenderContextImpl : KorenderContext {

        val appResourceLoader: ResourceLoader = this@Engine.appResourceLoader
        var currentRetentionPolicy: RetentionPolicy = TimeRetentionPolicy(10f)
        var currentRetentionGeneration: Int = 0
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

        override fun <T> load(resource: String, mapper: (ByteArray) -> T): Deferred<T> =
            CoroutineScope(Dispatchers.Default).async { mapper(appResourceLoader(resource)) }

        override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
            ResourceTextureDeclaration(textureResource, filter, wrap, aniso, currentRetentionPolicy)

        override fun texture(id: String, image: Image, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
            ImageTextureDeclaration(id, image as InternalImage, filter, wrap, aniso, currentRetentionPolicy)

        override fun texture3D(id: String, image: Image3D, filter: TextureFilter, wrap: TextureWrap, aniso: Int): Texture3DDeclaration =
            ImageTexture3DDeclaration(id, image as InternalImage3D, filter, wrap, aniso, currentRetentionPolicy)

        override fun textureProbe(frameProbeName: String): TextureDeclaration = ProbeTextureDeclaration(frameProbeName)

        override fun textureArray(vararg textureResources: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
            ResourceTextureArrayDeclaration(textureResources.toList(), filter, wrap, aniso, currentRetentionPolicy)

        override fun textureArray(id: String, images: TextureArrayImages, filter: TextureFilter, wrap: TextureWrap, aniso: Int): TextureArrayDeclaration =
            ImageTextureArrayDeclaration(id, images, filter, wrap, aniso, currentRetentionPolicy)

        override fun cubeTexture(resources: CubeTextureResources) = ResourceCubeTextureDeclaration(resources, currentRetentionPolicy)

        override fun cubeTexture(id: String, images: CubeTextureImages) = ImageCubeTextureDeclaration(id, images, currentRetentionPolicy)

        override fun cubeTextureProbe(envProbeName: String): CubeTextureDeclaration = ProbeCubeTextureDeclaration(envProbeName)

        override fun captureEnv(resolution: Int, near: Float, far: Float, position: Vec3, insideOut: Boolean, block: FrameContext.() -> Unit): Deferred<CubeTextureImages> {
            val sd = SceneDeclaration()
            block.invoke(DefaultFrameContext(kc, sd, FrameInfo(0, 0f, 0f, 0f, 0)))
            val images = CompletableDeferred<CubeTextureImages>()
            val startNano = Platform.nanoTime()

            val scene = Scene(sd, inventory, renderContext, kc.currentRetentionPolicy)
            fun tryRender(): Boolean {
                scene.renderToEnvProbe(EnvCaptureContext(resolution, position, near, far, insideOut, sd), "#immediate")
                    ?.fetch()
                    ?.let {
                        images.complete(it)
                        println("Capture env done in ${(Platform.nanoTime() - startNano) * 1e-9}s")
                        return true
                    }
                println("Capturing env not complete, retrying...")
                return false
            }

            fun cycle() {
                inventory.onWaitUpdate {
                    preFrames.addLast {
                        if (!tryRender()) {
                            cycle()
                        }
                    }
                }
            }
            if (!tryRender()) {
                cycle()
            }
            return images
        }

        override fun captureFrame(width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameContext.() -> Unit): Deferred<Image> {
            val sd = SceneDeclaration()
            block.invoke(DefaultFrameContext(kc, sd, FrameInfo(0, 0f, 0f, 0f, 0)))
            val image = CompletableDeferred<Image>()
            val startNano = Platform.nanoTime()

            val scene = Scene(sd, inventory, renderContext, kc.currentRetentionPolicy)
            fun tryRender(): Boolean {
                scene.renderToFrameProbe(FrameCaptureContext(width, height, camera as Camera, projection as Projection, sd), "#immediate")
                    ?.fetch()
                    ?.let {
                        image.complete(it)
                        println("Capture frame done in ${(Platform.nanoTime() - startNano) * 1e-9}s")
                        return true
                    }
                println("Capturing frame not complete, retrying...")
                return false
            }

            fun cycle() {
                inventory.onWaitUpdate {
                    preFrames.addLast {
                        if (!tryRender()) {
                            cycle()
                        }
                    }
                }
            }
            if (!tryRender()) {
                cycle()
            }
            return image
        }

        override fun quad(halfSideX: Float, halfSideY: Float) = Quad(halfSideX, halfSideY, currentRetentionPolicy)

        override fun biQuad(halfSideX: Float, halfSideY: Float) = BiQuad(halfSideX, halfSideY, currentRetentionPolicy)

        override fun cube(halfSide: Float) = Cube(halfSide, currentRetentionPolicy)

        override fun sphere(radius: Float, slices: Int, sectors: Int) = Sphere(radius, slices, sectors, currentRetentionPolicy)

        override fun cylinderSide(height: Float, radius: Float, sectors: Int) = CylinderSide(height, radius, sectors, currentRetentionPolicy)

        override fun coneTop(height: Float, radius: Float, sectors: Int) = ConeTop(height, radius, sectors, currentRetentionPolicy)

        override fun disk(radius: Float, sectors: Int) = Disk(radius, sectors, currentRetentionPolicy)

        override fun obj(objFile: String): MeshDeclaration = ObjMesh(objFile, currentRetentionPolicy)

        override fun customMesh(id: String, vertexCount: Int, indexCount: Int, vararg attributes: MeshAttribute<*>, dynamic: Boolean, indexType: IndexType?, block: MeshInitializer.() -> Unit): MeshDeclaration =
            CustomMesh(id, vertexCount, indexCount, attributes.asList(), dynamic, indexType, currentRetentionPolicy, block)

        override fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration =
            HeightField(id, cellsX, cellsZ, cellWidth, height, currentRetentionPolicy)


        override fun mutableMesh(): MutableMesh =
            InternalMutableMesh()

        override fun mesh(id: String, mesh: Mesh) =
            CustomCpuMesh(id, mesh, currentRetentionPolicy)

        override fun loadMesh(meshDeclaration: MeshDeclaration): Deferred<Mesh> =
            Geometry.loadCpuMesh(meshDeclaration, appResourceLoader)

        override fun pipeMesh(id: String, segments: Int, dynamic: Boolean, block: PipeMeshContext.() -> Unit) =
            createPipeMesh(id, segments, dynamic, currentRetentionPolicy, block)

        override fun customMaterial(vertShaderFile: String, fragShaderFile: String, block: MaterialContext.() -> Unit) =
            InternalMaterial(vertShaderFile, fragShaderFile).also { block.invoke(it) }

        override fun customMaterial(vertShaderFile: String, block: BaseMaterialContext.() -> Unit) =
            InternalBaseMaterial(vertShaderFile).also { block.invoke(it) }

        override fun base(block: BaseMaterialContext.() -> Unit) =
            InternalBaseMaterial().also { block.invoke(it) }

        override fun billboard(block: BillboardMaterialContext.() -> Unit) =
            InternalBillboardMaterial().also { block.invoke(it) }

        override fun terrain(block: TerrainMaterialContext.() -> Unit) =
            InternalTerrainMaterial().also { block.invoke(it) }

        override fun pipe(block: BaseMaterialContext.() -> Unit) =
            InternalPipeMaterial().also { block.invoke(it) }

        override fun decal(block: BaseMaterialContext.() -> Unit) =
            InternalDecalMaterial().also { block.invoke(it) }

        override fun blurVert(radius: Float) =
            BlurMaterial(true, radius)

        override fun blurHorz(radius: Float) =
            BlurMaterial(false, radius)

        override fun blur(radius: Float): PostProcessingEffect =
            simpleBlur(renderContext, radius)

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

        override fun customPostProcessingFilter(fragmentShaderFile: String, block: PostProcessMaterialContext.() -> Unit) =
            InternalPostProcessingMaterial(fragmentShaderFile).also { block.invoke(it) }

        override fun fastCloudSky(density: Float, thickness: Float, scale: Float, rippleAmount: Float, rippleScale: Float, zenithColor: ColorRGB, horizonColor: ColorRGB, cloudLight: Float, cloudDark: Float, block: MaterialContext.() -> Unit) =
            FastCloudSkyMaterial(density, thickness, scale, rippleAmount, rippleScale, zenithColor, horizonColor, cloudLight, cloudDark, block)

        override fun starrySky(colorness: Float, density: Float, speed: Float, size: Float, block: MaterialContext.() -> Unit) =
            StarrySkyMaterial(colorness, density, speed, size, block)

        override fun cubeSky(cubeTexture: CubeTextureDeclaration, block: MaterialContext.() -> Unit) =
            CubeSkyMaterial(cubeTexture, block)

        override fun textureSky(texture: TextureDeclaration, block: MaterialContext.() -> Unit) =
            TextureSkyMaterial(texture, block)

        override fun fog(density: Float, color: ColorRGB) =
            FogMaterial(density, color)

        override fun ssr(downsample: Int, maxReflectionDistance: Float, linearSteps: Int, binarySteps: Int, lastStepRatio: Float, envTexture: CubeTextureDeclaration?) =
            ssrEffect(downsample, maxReflectionDistance, linearSteps, binarySteps, lastStepRatio, envTexture, renderContext, currentRetentionPolicy)

        override fun bloom(threshold: Float, amount: Float, radius: Float, downsample: Int) =
            bloomSimpleEffect(renderContext, currentRetentionPolicy, threshold, amount, radius, downsample)

        override fun bloomWide(threshold: Float, amount: Float, downsample: Int, mips: Int, offset: Float, highResolutionRatio: Float) =
            bloomMipEffect(renderContext, currentRetentionPolicy, threshold, amount, downsample, mips, offset, highResolutionRatio)

        override fun projection(width: Float, height: Float, near: Float, far: Float, mode: ProjectionMode) =
            Projection(width, height, near, far, mode)

        override fun frustum() = FrustumProjectionMode

        override fun ortho() = OrthoProjectionMode

        override fun log(c: Float) = LogProjectionMode(c)

        override fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration =
            DefaultCamera(position, direction.normalize(), up.normalize())

        override
        var retentionPolicy: RetentionPolicy
            get() = currentRetentionPolicy
            set(value) {
                currentRetentionPolicy = value
            }

        override
        var retentionGeneration: Int
            get() = currentRetentionGeneration
            set(value) {
                currentRetentionGeneration = value
            }

        override
        var camera: CameraDeclaration
            get() = renderContext.camera
            set(value) {
                renderContext.camera = value as Camera
            }

        override
        var projection: ProjectionDeclaration
            get() = renderContext.projection
            set(value) {
                renderContext.projection = value as Projection
            }

        override
        var background: ColorRGBA
            get() = renderContext.backgroundColor
            set(value) {
                renderContext.backgroundColor = value
            }

        override
        val width: Int
            get() = renderContext.width

        override
        val height: Int
            get() = renderContext.height

        override fun createImage(width: Int, height: Int, format: PixelFormat): Image =
            Platform.createImage(width, height, format)

        override fun createImage3D(width: Int, height: Int, depth: Int, format: PixelFormat): Image3D =
            InternalImage3D(width, height, depth, NativeByteBuffer(width * height * depth * format.bytes), format)

        override fun loadImage(imageResource: String): Deferred<Image> = CoroutineScope(Dispatchers.Default).async {
            val bytes = appResourceLoader(imageResource)
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

        override fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab<TerrainMaterial> =
            Clipmaps(this, id, cellSize, hg, rings)

        override fun instancing(id: String, count: Int, dynamic: Boolean, block: InstancedRenderablesContext.() -> Unit) =
            InternalInstancingDeclaration(id, count, dynamic) {
                val instances = mutableListOf<MeshInstance>()
                val context = DefaultInstancedRenderablesContext(instances)
                block.invoke(context)
                instances
            }

        override fun billboardInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedBillboardsContext.() -> Unit) =
            InternalBillboardInstancingDeclaration(id, count, dynamic) {
                val instances = mutableListOf<BillboardInstance>()
                val context = DefaultInstancedBillboardsContext(instances)
                block.invoke(context)
                instances
            }

        override fun gltfInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedGltfContext.() -> Unit) =
            InternalGltfInstancingDeclaration(id, count, dynamic) {
                val instances = mutableListOf<GltfInstance>()
                val context = DefaultInstancedGltfContext(instances)
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
        val COLORTEXINDEX = MeshAttributes.COLORTEXINDEX

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
        preFrames.removeFirstOrNull()?.let { it() }
        val frameInfo = renderContext.frameInfoManager.frame(inventory.pending())
        processTouches()
        processKeys()
        val sd = SceneDeclaration()
        frameBlocks.forEach {
            DefaultFrameContext(kc, sd, frameInfo).apply(it)
        }
        inventory.go(frameInfo.time, kc.currentRetentionGeneration) {
            val loader = sd.loaderSceneDeclaration?.let { Scene(it, inventory, renderContext, kc.currentRetentionPolicy) }
            if (loader != null && !loaderLoaded) {
                loaderLoaded = loader.render() || inventory.pending() > 0
                loaderLoaded
            } else {
                val scene = Scene(sd, inventory, renderContext, kc.currentRetentionPolicy)
                val renderOk = scene.render()
                if (loader != null && (!renderOk || inventory.pending() > 0)) {
                    loader.render()
                }
                touchBoxes = scene.touchBoxes
                renderOk
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
        renderContext.width = w
        renderContext.height = h
    }
}



