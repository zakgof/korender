package com.zakgof.korender.context

import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.CubeTextureResources
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.IndexType
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.MaterialModifier
import com.zakgof.korender.Mesh
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MeshDeclaration
import com.zakgof.korender.MeshInitializer
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.PostProcessingEffect
import com.zakgof.korender.PostShadingEffect
import com.zakgof.korender.Prefab
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.Texture3DDeclaration
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import kotlinx.coroutines.Deferred

interface KorenderContext {

    fun Frame(block: FrameContext.() -> Unit)
    fun OnTouch(handler: TouchHandler)
    fun OnKey(handler: KeyHandler)

    var camera: CameraDeclaration
    var projection: ProjectionDeclaration
    var background: ColorRGBA
    var retentionPolicy: RetentionPolicy
    var retentionGeneration: Int

    /** Viewport width */
    val width: Int

    /** Viewport height */
    val height: Int

    /**
     * Helper method to load a resource file into an object.
     *
     * @param resource resource file name
     * @param mapper: function to instantiate object representation from the raw file's bytes
     * @return deferred object
     */
    fun <T> load(resource: String, mapper: (ByteArray) -> T): Deferred<T>

    /**
     * Creates a texture declaration from a resource image file.
     *
     * @param textureResource resource file name (png, jpg)
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return texture declaration
     */
    fun texture(textureResource: String, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration

    /**
     * Creates a texture declaration from an Image object.
     *
     * @param id unique declaration id
     * @param image Image object
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return texture declaration
     */
    fun texture(id: String, image: Image, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): TextureDeclaration

    /**
     * Creates a texture declaration from an frame probe.
     *
     * Frame probes can be created using CaptureFrame
     *
     * @param frameProbeName frame probe name
     * @return texture declaration
     */
    fun textureProbe(frameProbeName: String): TextureDeclaration

    /**
     * Creates a 3D texture from an Image3D object.
     *
     * @param id unique declaration id
     * @param image Image3D object
     * @param filter texture filtering mode
     * @param wrap texture wrapping mode
     * @param aniso anisotropy factor
     * @return 3D texture declaration
     */
    fun texture3D(id: String, image: Image3D, filter: TextureFilter = TextureFilter.MipMap, wrap: TextureWrap = TextureWrap.Repeat, aniso: Int = 1024): Texture3DDeclaration

    /**
     * Creates a cube texture declaration from resource files.
     *
     * Cube texture is a set of 6 textures representing cube sides.
     *
     * @param resources CubeTextureResources map containing image resource file name for every cube side
     * @return cube texture declaration
     */
    fun cubeTexture(resources: CubeTextureResources): CubeTextureDeclaration

    /**
     * Creates a cube texture declaration from Image objects
     *
     * Cube texture is a set of 6 textures representing cube sides.
     *
     * @param id unique declaration id
     * @param images CubeTextureImages map containing image object for every cube side
     * @return cube texture declaration
     */
    fun cubeTexture(id: String, images: CubeTextureImages): CubeTextureDeclaration

    /**
     * Creates a cube texture declaration from an environment probe.
     *
     * Cube texture is a set of 6 textures representing cube sides.
     * Environment probes can be created with CaptureEnv.
     *
     * @param envProbeName environment probe name
     * @return cube texture declaration
     */
    fun cubeTextureProbe(envProbeName: String): CubeTextureDeclaration

    /**
     * Captures scene into a cube texture images.
     *
     * Captures a scene into 6 frames images representing cube sides from the specified point.
     *
     * @param resolution each frame resolution in pixels (both width and height)
     * @param near distance from camera to near clipping plane
     * @param far distance from camera to far clipping plane
     * @param position camera position
     * @param insideOut experimental option to capture radial mapping when enabled
     * @param block scene to capture
     * @return deferred set of 6 images (per each cube side)
     */
    fun captureEnv(resolution: Int, near: Float, far: Float, position: Vec3 = ZERO, insideOut: Boolean = false, block: FrameContext.() -> Unit): Deferred<CubeTextureImages>

    /**
     * Captures scene into a frame image.
     *
     * @param width frame width in pixels
     * @param height frame height in pixels
     * @param camera camera declaration for frame capture
     * @param projection projection declaration for frame capture
     * @param block scene to capture
     * @return deferred frame image
     */
    fun captureFrame(width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameContext.() -> Unit): Deferred<Image>

    /**
     * Creates a quad mesh declaration.
     *
     * Creates z-axis facing quad consisting of two triangles.
     *
     * @param halfSideX half of quad width (in x dimension)
     * @param halfSideY half of quad height (in y dimension)
     * @return mesh declaration
     */
    fun quad(halfSideX: Float = 0.5f, halfSideY: Float = 0.5f): MeshDeclaration

    /**
     * Creates a two-sided quad mesh declaration.
     *
     * Creates a quad in XY visible from both sides.
     *
     * @param halfSideX half of quad width (in x dimension)
     * @param halfSideY half of quad height (in y dimension)
     * @return mesh declaration
     */
    fun biQuad(halfSideX: Float = 0.5f, halfSideY: Float = 0.5f): MeshDeclaration

    /**
     * Creates a cube mesh declaration.
     *
     * @param halfSide half of cube edge length
     * @return mesh declaration
     */
    fun cube(halfSide: Float = 0.5f): MeshDeclaration

    /**
     * Creates a sphere mesh declaration.
     *
     * @param radius sphere radius
     * @param slices number of horizontal slices (along parallels)
     * @param sectors number of vertical sector slices (along meridians)
     * @return mesh declaration
     */
    fun sphere(radius: Float = 1.0f, slices: Int = 32, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a disk mesh declaration (in xz plane).
     *
     * @param radius disk radius
     * @param sectors number of sector slices
     * @return mesh declaration
     */
    fun disk(radius: Float = 1f, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a conical surface mesh declaration (a cone without base).
     *
     * @param height cone height (in y direction)
     * @param radius base radius (base is in xz plane)
     * @param sectors number of slices
     * @return mesh declaration
     */
    fun coneTop(height: Float = 1f, radius: Float = 1f, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a cylindrical surface mesh declaration (a cylinder without bases).
     *
     * @param height cylinder height (in y direction)
     * @param radius base radius (base is in xz plane)
     * @param sectors number of stripes
     * @return mesh declaration
     */
    fun cylinderSide(height: Float = 1f, radius: Float = 1f, sectors: Int = 32): MeshDeclaration

    /**
     * Creates a heightfield mesh declaration.
     *
     * @param id unique declaration id
     * @param cellsX number of cells in x direction
     * @param cellsZ number of cells in z direction
     * @param cellWidth cell width (in x and z)
     * @param height height function (in y) by cell index in x and z
     * @return mesh declaration
     */
    fun heightField(id: String, cellsX: Int, cellsZ: Int, cellWidth: Float, height: (Int, Int) -> Float): MeshDeclaration

    /**
     * Creates a mesh declaration for a wavefront .obj resource file.
     *
     * Only loads a single mesh without materials.
     *
     * @param objFile .obj resource file name
     * @return mesh declaration
     */
    fun obj(objFile: String): MeshDeclaration

    /**
     * Creates a mesh declaration for a shape consisting of multiple connected cylinders (pipes)
     *
     * @param id unique declaration id
     * @param segments number of pipe segments
     * @param dynamic set to true if the content can change frame to frame
     * @param block pipe segment declaration block
     * @return mesh declaration
     */
    fun pipeMesh(id: String, segments: Int, dynamic: Boolean = false, block: PipeMeshContext.() -> Unit): MeshDeclaration

    /**
     * Creates a mesh declaration from a Mesh object.
     *
     * @param id unique declaration id
     * @param mesh Mesh object
     */
    fun mesh(id: String, mesh: Mesh): MeshDeclaration

    /**
     * Creates a mesh declaration from an indexed triangle list.
     *
     * @param id unique declaration id
     * @param vertexCount number of vertices
     * @param indexCount number of indices
     * @param attributes mesh vertex attributes
     * @param dynamic set to true if content can change frame to frame
     * @param indexType index type (omit for auto)
     * @param block block declaring mesh vertices and indices
     * @return mesh declaration
     */
    fun customMesh(id: String, vertexCount: Int, indexCount: Int, vararg attributes: MeshAttribute<*>, dynamic: Boolean = false, indexType: IndexType? = null, block: MeshInitializer.() -> Unit): MeshDeclaration

    /**
     * Creates a material modifier that applies a custom vertex shader.
     *
     * @param vertShaderFile vertex shader resource file
     * @return material modifier
     */
    fun vertex(vertShaderFile: String): MaterialModifier

    /**
     * Creates a material modifier that applies a custom fragment shader.
     *
     * @param fragShaderFile fragment shader resource file
     * @return material modifier
     */
    fun fragment(fragShaderFile: String): MaterialModifier

    /**
     * Creates a material modifier that applies shader definitions.
     *
     * Definitions affect the #ifdef / #ifndef directives in shaders
     *
     * @param defs fragment shader defines
     * @return material modifier
     */
    fun defs(vararg defs: String): MaterialModifier

    /**
     * Creates a material modifier that applies a shader plugin.
     *
     * @param name plugin mount point name
     * @param shaderFile plugin shader code resource file
     * @return material modifier
     */
    fun plugin(name: String, shaderFile: String): MaterialModifier

    /**
     * Creates a material modifier that applies raw uniform parameters.
     *
     * @param pairs pairs uniform name to uniform value
     * @return material modifier
     */
    fun uniforms(vararg pairs: Pair<String, Any?>): MaterialModifier

    /**
     * Creates a material modifier for standard material.
     *
     * @param color albedo color
     * @param colorTexture albedo texture (multiplied by color)
     * @param metallicFactor metallic factor in PBR model
     * @param roughnessFactor roughness factor in PBR model
     * @param alphaCutoff alpha value threshold to discard fragments
     * @return material modifier
     */
    fun base(color: ColorRGBA = ColorRGBA.White, colorTexture: TextureDeclaration? = null, metallicFactor: Float = 0.1f, roughnessFactor: Float = 0.5f, alphaCutoff: Float = 0.01f): MaterialModifier

    /**
     * Creates a material modifier that applies triplanar rendering.
     * Used with the base material.
     *
     * @param scale scale for mapping world coordinates into texture coordinates.
     * @return material modifier
     */
    fun triplanar(scale: Float = 1.0f): MaterialModifier

    /**
     * Creates a material modifier that applies normal mapping.
     * Used with the base material.
     *
     * @param normalTexture normal mapping texture declaration
     * @return material modifier
     */
    fun normalTexture(normalTexture: TextureDeclaration): MaterialModifier

    /**
     * Creates a material modifier that applies light emission.
     * Used with the base material.
     *
     * @param factor emission factor (color)
     * @return material modifier
     */
    fun emission(factor: ColorRGB): MaterialModifier

    /**
     * Creates a material modifier that applies metallic and roughness texture.
     * Used with the base material.
     *
     * @param texture metallic-roughness texture (r channel for metallic, g for roughness)
     * @return material modifier
     */
    fun metallicRoughnessTexture(texture: TextureDeclaration): MaterialModifier

    /**
     * Creates a material modifier that applies specular-glossiness PBR model flavor.
     * Used with the base material.
     *
     * @param specularFactor specular factor
     * @param glossinessFactor glossiness factor
     * @return material modifier
     */
    fun specularGlossiness(specularFactor: ColorRGB, glossinessFactor: Float): MaterialModifier

    /**
     * Creates a material modifier that applies specular-glossiness PBR model flavor.
     * Used with the base material.
     *
     * @param texture specular-glossiness texture (r channel for specular, g for glossiness)
     * @return material modifier
     */
    fun specularGlossinessTexture(texture: TextureDeclaration): MaterialModifier

    /**
     * Creates a material modifier for billboards.
     * Used with the base material.
     *
     * @param position billboard center position
     * @param scale billboard quad size
     * @param rotation rotation angle
     * @return material modifier
     */
    fun billboard(position: Vec3 = ZERO, scale: Vec2 = Vec2(1f, 1f), rotation: Float = 0.0f): MaterialModifier

    fun terrain(heightTexture: TextureDeclaration, heightTextureSize: Int, heightScale: Float, outsideHeight: Float, terrainCenter: Vec3 = ZERO): MaterialModifier
    fun pipe(): MaterialModifier
    fun radiant(radiantTexture: CubeTextureDeclaration, radiantNormalTexture: CubeTextureDeclaration, colorTexture: CubeTextureDeclaration, normalTexture: CubeTextureDeclaration): MaterialModifier

    fun radiantCapture(radiantMax: Float): MaterialModifier
    fun normalCapture(): MaterialModifier

    fun blur(radius: Float): PostProcessingEffect
    fun blurHorz(radius: Float): MaterialModifier
    fun blurVert(radius: Float): MaterialModifier
    fun adjust(brightness: Float = 0.0f, contrast: Float = 1.0f, saturation: Float = 1.0f): MaterialModifier
    fun water(waterColor: ColorRGB = ColorRGB(0x00182A), transparency: Float = 0.1f, waveScale: Float = 25.0f, waveMagnitude: Float = 0.3f): MaterialModifier
    fun fog(density: Float = 0.00003f, color: ColorRGB = ColorRGB(0xB8CAE9)): MaterialModifier
    fun fxaa(): MaterialModifier

    fun fire(strength: Float = 3.0f): MaterialModifier
    fun fireball(power: Float = 0.5f): MaterialModifier
    fun smoke(density: Float = 0.5f, seed: Float = 0f): MaterialModifier

    fun fastCloudSky(
        density: Float = 3.0f,
        thickness: Float = 10.0f,
        scale: Float = 1.0f,
        rippleAmount: Float = 0.3f,
        rippleScale: Float = 4.0f,
        zenithColor: ColorRGB = ColorRGB(0x3F6FC3),
        horizonColor: ColorRGB = ColorRGB(0xB8CAE9),
        cloudLight: Float = 1.0f,
        cloudDark: Float = 0.7f
    ): MaterialModifier

    fun starrySky(colorness: Float = 0.8f, density: Float = 20.0f, speed: Float = 1.0f, size: Float = 15.0f): MaterialModifier
    fun cubeSky(cubeTexture: CubeTextureDeclaration): MaterialModifier
    fun textureSky(texture: TextureDeclaration): MaterialModifier

    fun ibl(env: CubeTextureDeclaration): MaterialModifier
    fun ibl(env: MaterialModifier): MaterialModifier

    fun roiTextures(block: RoiTexturesContext.() -> Unit): MaterialModifier

    fun ssr(width: Int? = null, height: Int? = null, fxaa: Boolean = false, maxRayTravel: Float = 10f, linearSteps: Int = 12, binarySteps: Int = 5, envTexture: CubeTextureDeclaration? = null): PostShadingEffect
    fun bloom(threshold: Float = 0.9f, amount: Float = 3.0f, radius: Float = 16f, downsample: Int = 2): PostShadingEffect
    fun bloomWide(threshold: Float = 0.9f, amount: Float = 3.0f, downsample: Int = 2, mips: Int = 3, offset: Float = 1.0f, highResolutionRatio: Float = 0.2f): PostShadingEffect

    fun projection(width: Float, height: Float, near: Float, far: Float, mode: ProjectionMode = frustum()): ProjectionDeclaration
    fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration
    fun frustum(): ProjectionMode
    fun ortho(): ProjectionMode
    fun log(c: Float = 1.0f): ProjectionMode

    fun createImage(width: Int, height: Int, format: PixelFormat): Image
    fun loadImage(imageResource: String): Deferred<Image>
    fun loadImage(bytes: ByteArray, type: String): Deferred<Image>

    fun createImage3D(width: Int, height: Int, depth: Int, format: PixelFormat): Image3D

    fun vsm(blurRadius: Float? = null): ShadowAlgorithmDeclaration
    fun hard(): ShadowAlgorithmDeclaration
    fun softwarePcf(samples: Int = 8, blurRadius: Float = 0.005f, bias: Float = 0.0005f): ShadowAlgorithmDeclaration
    fun hardwarePcf(bias: Float = 0.005f): ShadowAlgorithmDeclaration

    fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab

    fun instancing(id: String, count: Int, dynamic: Boolean, block: InstancedRenderablesContext.() -> Unit): InstancingDeclaration
    fun gltfInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedGltfContext.() -> Unit): GltfInstancingDeclaration
    fun billboardInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedBillboardsContext.() -> Unit): BillboardInstancingDeclaration

    fun immediatelyFree(): RetentionPolicy
    fun keepForever(): RetentionPolicy
    fun untilGeneration(generation: Int): RetentionPolicy
    fun time(seconds: Float): RetentionPolicy

    val target: TargetPlatform

    enum class TargetPlatform {
        Desktop,
        Android,
        Web
    }
}
