package com.zakgof.korender.impl.engine

import com.zakgof.korender.AsyncContext
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.Image
import com.zakgof.korender.IndexType
import com.zakgof.korender.KeyEvent
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.KorenderException
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.Platform
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShadowAlgorithmDeclaration
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
import com.zakgof.korender.context.RoiTexturesContext
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
import com.zakgof.korender.impl.geometry.HeightField
import com.zakgof.korender.impl.geometry.ObjMesh
import com.zakgof.korender.impl.geometry.Quad
import com.zakgof.korender.impl.geometry.Sphere
import com.zakgof.korender.impl.gl.GL.glEnable
import com.zakgof.korender.impl.gl.GLConstants.GL_TEXTURE_CUBE_MAP_SEAMLESS
import com.zakgof.korender.impl.ignoringGlError
import com.zakgof.korender.impl.image.InternalImage
import com.zakgof.korender.impl.material.ImageCubeTextureDeclaration
import com.zakgof.korender.impl.material.ImageTextureDeclaration
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.InternalPostShadingEffect
import com.zakgof.korender.impl.material.InternalRoiTexturesContext
import com.zakgof.korender.impl.material.ProbeCubeTextureDeclaration
import com.zakgof.korender.impl.material.ProbeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceCubeTextureDeclaration
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.impl.prefab.terrain.Clipmaps
import com.zakgof.korender.impl.projection.FrustumProjectionMode
import com.zakgof.korender.impl.projection.LogProjectionMode
import com.zakgof.korender.impl.projection.OrthoProjectionMode
import com.zakgof.korender.impl.projection.Projection
import com.zakgof.korender.impl.resourceBytes
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    private var touchBoxes: List<TouchBox> = listOf()
    private var pressedTouchBoxIds = setOf<Any>()
    private val touchHandlers = mutableListOf<TouchHandler>()
    private val keyHandlers = mutableListOf<KeyHandler>()
    private val kc = KorenderContextImpl()
    private var loaderLoaded = false

    inner class KorenderContextImpl : KorenderContext {

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
            CoroutineScope(Dispatchers.Default).async { mapper(asyncContext.appResourceLoader(resource)) }

        override fun texture(textureResource: String, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
            ResourceTextureDeclaration(textureResource, filter, wrap, aniso, currentRetentionPolicy)

        override fun texture(id: String, image: Image, filter: TextureFilter, wrap: TextureWrap, aniso: Int) =
            ImageTextureDeclaration(id, image as InternalImage, filter, wrap, aniso, currentRetentionPolicy)

        override fun textureProbe(frameProbeName: String): TextureDeclaration = ProbeTextureDeclaration(frameProbeName)

        override fun cubeTexture(resources: CubeTextureResources) = ResourceCubeTextureDeclaration(resources, currentRetentionPolicy)

        override fun cubeTexture(id: String, images: CubeTextureImages) = ImageCubeTextureDeclaration(id, images, currentRetentionPolicy)

        override fun cubeTextureProbe(envProbeName: String): CubeTextureDeclaration = ProbeCubeTextureDeclaration(envProbeName)

        override fun captureEnv(resolution: Int, near: Float, far: Float, position: Vec3, insideOut: Boolean, block: FrameContext.() -> Unit): CubeTextureImages {
            val sd = SceneDeclaration()
            block.invoke(DefaultFrameContext(kc, sd, FrameInfo(0, 0f, 0f, 0f, 0)))
            var images: CubeTextureImages? = null
            inventory.go(0f, 0) {
                val scene = Scene(sd, inventory, renderContext, kc.currentRetentionPolicy)
                while (true) {
                    val cubeTexture = scene.renderToEnvProbe(EnvCaptureContext(resolution, position, near, far, insideOut, sd), "#immediate")
                    if (cubeTexture != null) {
                        images = cubeTexture.fetch()
                        println("Fetch done " + inventory.pending())
                        break
                    }
                    println("Resources pending, retrying env capture... " + inventory.pending())
                }
                true
            }
            return images!!
        }

        override fun captureFrame(width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameContext.() -> Unit): Image {
            val sd = SceneDeclaration()
            block.invoke(DefaultFrameContext(kc, sd, FrameInfo(0, 0f, 0f, 0f, 0)))
            var image: Image? = null
            inventory.go(0f, 0) {
                val scene = Scene(sd, inventory, renderContext, kc.currentRetentionPolicy)
                while (true) {
                    val texture = scene.renderToFrameProbe(FrameCaptureContext(width, height, camera as Camera, projection as Projection, sd), "#immediate")
                    if (texture != null) {
                        image = texture.fetch()
                        break
                    }
                    println("Resources pending, retrying frame capture... " + inventory.pending())
                }
                true
            }
            return image!!
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

        override fun mesh(id: String, mesh: Mesh) =
            CustomCpuMesh(id, mesh, currentRetentionPolicy)

        override fun vertex(vertShaderFile: String): InternalMaterialModifier =
            InternalMaterialModifier { it.vertShaderFile = vertShaderFile }

        override fun fragment(fragShaderFile: String): InternalMaterialModifier =
            InternalMaterialModifier { it.fragShaderFile = fragShaderFile }

        override fun defs(vararg defs: String): InternalMaterialModifier =
            InternalMaterialModifier { it.shaderDefs += setOf(*defs) }

        override fun plugin(name: String, shaderFile: String) = InternalMaterialModifier {
            it.plugins[name] = shaderFile
        }

        override fun base(color: ColorRGBA, colorTexture: TextureDeclaration?, metallicFactor: Float, roughnessFactor: Float) = InternalMaterialModifier {
            it.uniforms["baseColor"] = color
            it.uniforms["baseColorTexture"] = colorTexture
            it.uniforms["metallicFactor"] = metallicFactor
            it.uniforms["roughnessFactor"] = roughnessFactor
            if (colorTexture != null) {
                it.shaderDefs += "BASE_COLOR_MAP";
            }
        }

        override fun triplanar(scale: Float): MaterialModifier = InternalMaterialModifier {
            it.plugins["texturing"] = "!shader/plugin/texturing.triplanar.frag"
            it.uniforms["triplanarScale"] = scale
        }

        override fun normalTexture(normalTexture: TextureDeclaration) = InternalMaterialModifier {
            it.plugins["normal"] = "!shader/plugin/normal.texture.frag"
            it.uniforms["normalTexture"] = normalTexture
        }

        override fun emission(factor: ColorRGB): MaterialModifier = InternalMaterialModifier {
            it.plugins["emission"] = "!shader/plugin/emission.factor.frag"
            it.uniforms["emissionFactor"] = factor
        }

        override fun metallicRoughnessTexture(texture: TextureDeclaration) = InternalMaterialModifier {
            it.plugins["metallic_roughness"] = "!shader/plugin/metallic_roughness.texture.frag"
            it.uniforms["metallicRoughnessTexture"] = texture
        }

        override fun specularGlossiness(specularFactor: ColorRGB, glossinessFactor: Float) = InternalMaterialModifier {
            it.plugins["specular_glossiness"] = "!shader/plugin/specular_glossiness.factor.frag"
            it.uniforms["specularFactor"] = specularFactor
            it.uniforms["glossinessFactor"] = glossinessFactor
        }

        override fun specularGlossinessTexture(texture: TextureDeclaration) = InternalMaterialModifier {
            it.plugins["specular_glossiness"] = "!shader/plugin/specular_glossiness.texture.frag"
            it.uniforms["specularGlossinessTexture"] = texture
        }

        override fun billboard(position: Vec3, scale: Vec2, rotation: Float) = InternalMaterialModifier {
            it.uniforms["pos"] = position
            it.uniforms["scale"] = scale
            it.uniforms["rotation"] = rotation
        }

        override fun terrain(heightTexture: TextureDeclaration, heightTextureSize: Int, heightScale: Float, outsideHeight: Float, terrainCenter: Vec3) = InternalMaterialModifier {
            it.plugins["normal"] = "!shader/plugin/normal.terrain.frag"
            it.plugins["terrain"] = "!shader/plugin/terrain.texture.frag"
            it.uniforms["heightTexture"] = heightTexture
            it.uniforms["heightTextureSize"] = heightTextureSize
            it.uniforms["heightScale"] = heightScale
            it.uniforms["outsideHeight"] = outsideHeight
            it.uniforms["terrainCenter"] = terrainCenter
        }

        override fun radiant(radiantTexture: CubeTextureDeclaration, radiantNormalTexture: CubeTextureDeclaration, colorTexture: CubeTextureDeclaration, normalTexture: CubeTextureDeclaration) = InternalMaterialModifier {
            it.plugins["position"] = "!shader/plugin/position.radiant.frag"
            it.plugins["normal"] = "!shader/plugin/normal.radiant.frag"
            it.plugins["albedo"] = "!shader/plugin/albedo.radiant.frag"
            it.plugins["depth"] = "!shader/plugin/depth.radiant.frag"
            it.uniforms["radiantTexture"] = radiantTexture
            it.uniforms["radiantNormalTexture"] = radiantNormalTexture
            it.uniforms["colorCubeTexture"] = colorTexture
            it.uniforms["normalCubeTexture"] = normalTexture
        }

        override fun radiantCapture(radiantMax: Float) = InternalMaterialModifier {
            it.plugins["output"] = "!shader/plugin/output.radiant.frag"
            it.uniforms["radiantMax"] = radiantMax
        }

        override fun normalCapture() = InternalMaterialModifier {
            it.plugins["output"] = "!shader/plugin/output.normal.frag"
        }

        override fun uniforms(vararg pairs: Pair<String, Any?>) = InternalMaterialModifier { mb ->
            pairs.forEach {
                mb.uniforms[it.first] = it.second
            }
        }

        override fun blurVert(radius: Float) = InternalMaterialModifier {
            it.fragShaderFile = "!shader/effect/blurh.frag"
            it.uniforms["radius"] = radius
        }

        override fun blurHorz(radius: Float) = InternalMaterialModifier {
            it.fragShaderFile = "!shader/effect/blurv.frag"
            it.uniforms["radius"] = radius
        }

        override fun adjust(brightness: Float, contrast: Float, saturation: Float) = InternalMaterialModifier {
            it.fragShaderFile = "!shader/effect/adjust.frag"
            it.uniforms["brightness"] = brightness
            it.uniforms["contrast"] = contrast
            it.uniforms["saturation"] = saturation
        }

        override fun fire(strength: Float): MaterialModifier = InternalMaterialModifier {
            it.vertShaderFile = "!shader/billboard.vert"
            it.fragShaderFile = "!shader/effect/fire.frag"
            it.uniforms["strength"] = strength
        }

        override fun fireball(power: Float) = InternalMaterialModifier {
            it.vertShaderFile = "!shader/billboard.vert"
            it.fragShaderFile = "!shader/effect/fireball.frag"
            it.uniforms["power"] = power
        }

        override fun smoke(density: Float, seed: Float) = InternalMaterialModifier {
            it.vertShaderFile = "!shader/billboard.vert"
            it.fragShaderFile = "!shader/effect/smoke.frag"
            it.uniforms["density"] = density
            it.uniforms["seed"] = seed
        }

        override fun water(waterColor: ColorRGB, transparency: Float, waveScale: Float, waveMagnitude: Float) = InternalMaterialModifier {
            it.fragShaderFile = "!shader/effect/water.frag"
            it.uniforms["waterColor"] = waterColor
            it.uniforms["transparency"] = transparency
            it.uniforms["waveScale"] = waveScale
            it.uniforms["waveMagnitude"] = waveMagnitude
        }

        override fun fxaa() = InternalMaterialModifier {
            it.fragShaderFile = "!shader/effect/fxaa.frag"
        }

        override fun fastCloudSky(density: Float, thickness: Float, scale: Float, rippleAmount: Float, rippleScale: Float, zenithColor: ColorRGB, horizonColor: ColorRGB, cloudLight: Float, cloudDark: Float) = InternalMaterialModifier {
            it.plugins["sky"] = "!shader/plugin/sky.fastcloud.frag"
            it.uniforms["density"] = density
            it.uniforms["thickness"] = thickness
            it.uniforms["scale"] = scale
            it.uniforms["zenithcolor"] = zenithColor
            it.uniforms["horizoncolor"] = horizonColor
            it.uniforms["rippleamount"] = rippleAmount
            it.uniforms["ripplescale"] = rippleScale
            it.uniforms["cloudlight"] = cloudLight
            it.uniforms["clouddark"] = cloudDark
        }

        override fun starrySky(colorness: Float, density: Float, speed: Float, size: Float) = InternalMaterialModifier {
            it.plugins["sky"] = "!shader/plugin/sky.starry.frag"
            it.uniforms["colorness"] = colorness
            it.uniforms["density"] = density
            it.uniforms["speed"] = speed
            it.uniforms["size"] = size
        }

        override fun cubeSky(cubeTexture: CubeTextureDeclaration) =
            InternalMaterialModifier {
                it.plugins["sky"] = "!shader/plugin/sky.cube.frag"
                it.uniforms["cubeTexture"] = cubeTexture
            }

        override fun textureSky(texture: TextureDeclaration) =
            InternalMaterialModifier {
                it.plugins["sky"] = "!shader/plugin/sky.texture.frag"
                it.uniforms["skyTexture"] = texture
            }

        override fun fog(density: Float, color: ColorRGB) = InternalMaterialModifier {
            it.fragShaderFile = "!shader/effect/fog.frag"
            it.uniforms["density"] = density
            it.uniforms["fogColor"] = color
        }

        override fun ibl(env: CubeTextureDeclaration) = cubeSky(env)

        override fun ibl(env: MaterialModifier) = env

        override fun roiTextures(block: RoiTexturesContext.() -> Unit) = InternalMaterialModifier {
            it.shaderDefs += "ROI"
            InternalRoiTexturesContext().apply(block).collect(it)
        }

        override fun ssr(width: Int?, height: Int?, fxaa: Boolean, maxRayTravel: Float, linearSteps: Int, binarySteps: Int, envTexture: CubeTextureDeclaration?): PostShadingEffect {
            val w = width ?: renderContext.width
            val h = height ?: renderContext.height
            return InternalPostShadingEffect(
                "ssr", w, h,
                effectPassMaterialModifiers =
                    listOf(
                        InternalMaterialModifier {
                            it.fragShaderFile = "!shader/effect/ssr.frag"
                            it.uniforms["linearSteps"] = linearSteps
                            it.uniforms["binarySteps"] = binarySteps
                            it.uniforms["maxRayTravel"] = maxRayTravel
                            envTexture?.let { et ->
                                it.uniforms["envTexture"] = et
                                it.shaderDefs += "SSR_ENV"
                            }
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
                }, currentRetentionPolicy
            )
        }

        override fun bloom(width: Int?, height: Int?) = InternalPostShadingEffect(
            "bloom",
            width ?: renderContext.width,
            height ?: renderContext.height,
            effectPassMaterialModifiers = listOf(
                InternalMaterialModifier {
                    it.fragShaderFile = "!shader/effect/bloom.frag"
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
            }, currentRetentionPolicy
        )

        override fun projection(width: Float, height: Float, near: Float, far: Float, mode: ProjectionMode) =
            Projection(width, height, near, far, mode)

        override fun frustum() = FrustumProjectionMode

        override fun ortho() = OrthoProjectionMode

        override fun log(c: Float) = LogProjectionMode(c)

        override fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration =
            DefaultCamera(position, direction.normalize(), up.normalize())

        override var retentionPolicy: RetentionPolicy
            get() = currentRetentionPolicy
            set(value) {
                currentRetentionPolicy = value
            }

        override var retentionGeneration: Int
            get() = currentRetentionGeneration
            set(value) {
                currentRetentionGeneration = value
            }

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

        override var background: ColorRGBA
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

        override fun loadImage(imageResource: String): Deferred<Image> =
            asyncContext.call {
                val bytes = resourceBytes(asyncContext.appResourceLoader, imageResource)
                Platform.loadImage(bytes, imageResource.split(".").last()).await()
            }

        override fun vsm(blurRadius: Float?): ShadowAlgorithmDeclaration =
            InternalVsmShadow(blurRadius)

        override fun hard(): ShadowAlgorithmDeclaration =
            InternalHardShadow()

        override fun softwarePcf(samples: Int, blurRadius: Float): ShadowAlgorithmDeclaration =
            InternalSoftwarePcfShadow(samples, blurRadius)

        override fun hardwarePcf(bias: Float): ShadowAlgorithmDeclaration =
            InternalHardwarePcfShadow(bias)

        override fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab =
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
    }

    init {
        println("Engine init $width x $height")
        ignoringGlError {
            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS)
        }
        block.invoke(kc)
    }

    fun frame() {
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