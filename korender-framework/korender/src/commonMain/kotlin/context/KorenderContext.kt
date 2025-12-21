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

    /** Current camera */
    var camera: CameraDeclaration

    /** Current projection */
    var projection: ProjectionDeclaration

    /** Background clear color */
    var background: ColorRGBA

    /** Current object retention policy */
    var retentionPolicy: RetentionPolicy

    /** Current object retention generation */
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
     * Creates a material modifier that applies emission texture.
     * Used with the base material.
     *
     * @param texture emission texture
     * @return material modifier
     */
    fun emissionTexture(texture: TextureDeclaration): MaterialModifier

    /**
     * Creates a material modifier that applies a pre-calculated occlusion texture.
     * Used with the base material.
     *
     * @param texture occlusion texture
     * @return material modifier
     */
    fun occlusionTexture(texture: TextureDeclaration): MaterialModifier

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

    /**
     * Creates a material modifier for clipmap terrains.
     * Used with the base material.
     *
     * @param heightTexture texture declaration for the heightmap, must be square. Red channel is used for elevation value
     * @param heightScale height scale: world space terrain elevation value for max texture sample value
     * @param outsideHeight world space elevation valur for points outside the texture range
     * @param terrainCenter world space point corresponding to terrain center
     * @return material modifier
     */
    fun terrain(heightTexture: TextureDeclaration, heightScale: Float, outsideHeight: Float, terrainCenter: Vec3 = ZERO): MaterialModifier

    /**
     * Creates a material modifier for pipe shapes
     * Used with the base material.
     * @return material modifier
     */
    fun pipe(): MaterialModifier

    /**
     * Creates a material modifier for convex radiant mapping shapes
     * Used with the base material.
     * @param radiantTexture cube texture declaration representing distance from the object center to the surface at the given direction
     * @param radiantNormalTexture cube texture declaration representing object convex shape normal at the surface point at the given direction from the object center
     * @param radiantNormalTexture cube texture declaration representing albedo color of the surface point at the given direction to the object center
     * @param radiantNormalTexture cube texture declaration for local normal at the surface point at the given direction to the object center
     * @return material modifier
     */
    fun radiant(radiantTexture: CubeTextureDeclaration, radiantNormalTexture: CubeTextureDeclaration, colorTexture: CubeTextureDeclaration, normalTexture: CubeTextureDeclaration): MaterialModifier

    /**
     * Material modifier that outputs distance to origin when capturing environment probes
     * @param radiantMax max distance to origin
     * @return material modifier
     */
    fun radiantCapture(radiantMax: Float): MaterialModifier

    /**
     * Material modifier that outputs normal map when capturing environment probes.
     * @return material modifier
     */
    fun normalCapture(): MaterialModifier

    /**
     * Creates gaussian blur post-processing effect.
     * @param radius blur radius in pixels
     * @return post processing effect
     */
    fun blur(radius: Float): PostProcessingEffect

    /**
     * Creates one-dimensional gaussian blur material modifier in horizontal direction.
     * @param radius blur radius in pixels
     * @return material modifier
     */
    fun blurHorz(radius: Float): MaterialModifier

    /**
     * Creates one-dimensional gaussian blur material modifier in vertical direction.
     * @param radius blur radius in pixels
     * @return material modifier
     */
    fun blurVert(radius: Float): MaterialModifier

    /**
     * Creates material modifier to adjust scene's brighness/contract/saturation.
     * Used as a post processing effect.
     * @param brightness brightness adjustment, -1..1 (0 does not change)
     * @param contrast contrast adjustment, 0..infinity (1 does not change)
     * @param contrast saturation adjustment, 0..infinity (1 does not change)
     * @return material modifier
     */
    fun adjust(brightness: Float = 0.0f, contrast: Float = 1.0f, saturation: Float = 1.0f): MaterialModifier

    /**
     * Creates a water surface with waves as a post process material modifier.
     * Used as a post processing effect.
     *
     * @param waterColor base water color
     * @param transparency water transparency factor
     * @param waveScale horizontal scale factor for waves
     * @param waveMagnitude vertical scale factor for waves
     * @return material modifier
     */
    fun water(waterColor: ColorRGB = ColorRGB(0x00182A), transparency: Float = 0.1f, waveScale: Float = 25.0f, waveMagnitude: Float = 0.3f): MaterialModifier

    /**
     * Creates fog as material modifier.
     * Used as a post processing effect.
     *
     * @param density fog density factor
     * @param color fog color
     * @return material modifier
     */
    fun fog(density: Float = 0.00003f, color: ColorRGB = ColorRGB(0xB8CAE9)): MaterialModifier

    /**
     * Creates an FXAA material modifier.
     * Used as a post processing effect.
     * @return material modifier
     */
    fun fxaa(): MaterialModifier

    /**
     * Creates a fire effect.
     * Normally used on a billboard.
     * @param strength fire strength factor 1..5
     * @return material modifier
     */
    fun fire(strength: Float = 3.0f): MaterialModifier

    /**
     * Creates a fireball effect.
     * Normally used on a billboard.
     * @param power power fireball explosion phase 0..1
     * @return material modifier
     */
    fun fireball(power: Float = 0.5f): MaterialModifier

    /**
     * Creates a single smoke ball effect.
     * Normally used on a billboard.
     * @param density smoke density
     * @param seed seed for randomness (0..1)
     * @return material modifier
     */
    fun smoke(density: Float = 0.5f, seed: Float = 0f): MaterialModifier

    /**
     * Creates a cloudy sky material modifier.
     * @param density cloud density
     * @param thickness cloud thickness
     * @param scale cloud scale
     * @param rippleAmount rippleAmount
     * @param rippleScale rippleScale
     * @param zenithColor color at zenith
     * @param horizonColor color at horizon
     * @param cloudLight white intensity for light clouds
     * @param cloudLight white intensity for dark clouds
     * @return material modifier
     */
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

    /**
     * Creates a night sky with stars material modifier.
     * @param colorness saturation factor for stars
     * @param density factor for amount of stars
     * @param speed star motion speed factor
     * @param size star size factor
     * @return material modifier
     */
    fun starrySky(colorness: Float = 0.8f, density: Float = 20.0f, speed: Float = 1.0f, size: Float = 15.0f): MaterialModifier

    /**
     * Creates a sky material modifier from a cube texture.
     * @param cubeTexture cube texture declaration
     * @return material modifier
     */
    fun cubeSky(cubeTexture: CubeTextureDeclaration): MaterialModifier

    /**
     * Creates a sky material modifier from a flat texture.
     * @param texture texture declaration
     * @return material modifier
     */
    fun textureSky(texture: TextureDeclaration): MaterialModifier

    /**
     * Creates a material modifier that applies environment lighting.
     * Used with base material.
     * @param env cube texture declaration
     * @return material modifier
     */
    fun ibl(env: CubeTextureDeclaration): MaterialModifier

    /**
     * Creates a material modifier that applies environment lighting from a sky material modifier.
     * Used with base material.
     * @param env sky material modifier
     * @return material modifier
     */
    fun ibl(env: MaterialModifier): MaterialModifier

    /**
     * Creates a screen space reflection post shading effect for deferred rendering pipeline.
     * Experimental.
     * @param downsample downsample factor for SSR framebuffer (normally 1, 2 or 4)
     * @param maxReflectionDistance maximum distance a reflected ray traced
     * @param linearSteps number of forward raytracing steps
     * @param binarySteps number of binary search steps after the forward tracing
     * @param lastStepRatio factor to multiply forward raytracing step at maxReflectionDistance
     * @param envTexture cube texture declaration for environment reflections
     */
    fun ssr(downsample: Int = 2, maxReflectionDistance: Float = 10f, linearSteps: Int = 64, binarySteps: Int = 5, lastStepRatio: Float = 4f, envTexture: CubeTextureDeclaration? = null): PostShadingEffect

    /**
     * Creates a bloom (glow) post shading effect for deferred rendering pipeline.
     * Experimental.
     * @param threshold luminance threshold for pixels to glow
     * @param amount bloom intensity factor
     * @param radius bloom radius
     * @param downsample downsample factor for bloom framebuffer (normally 1, 2 or 4)
     * @return post shading effect
     */
    fun bloom(threshold: Float = 0.9f, amount: Float = 3.0f, radius: Float = 16f, downsample: Int = 2): PostShadingEffect

    /**
     * Creates a bloom (glow) post shading effect with Kawase blur for deferred rendering pipeline.
     * Use this variant for wider bloom areas.
     * Experimental.
     * @param threshold luminance threshold for pixels to glow
     * @param amount bloom intensity factor
     * @param downsample downsample factor for bloom framebuffer (normally 1, 2 or 4)
     * @param mips number of blur passes (normally 2-5)
     * @param offset downsampling kernel size
     * @param highResolutionRatio factor to include a higher-resolution buffer during the upsampling
     * @return post shading effect
     */
    fun bloomWide(threshold: Float = 0.9f, amount: Float = 3.0f, downsample: Int = 2, mips: Int = 3, offset: Float = 1.0f, highResolutionRatio: Float = 0.2f): PostShadingEffect

    /**
     * Creates a projection declaration.
     *
     * @param width width at near clipping plane
     * @param height height at near clipping plane
     * @param near distance from camera to near clipping plane
     * @param far distance from camera to far clipping plane
     * @param mode projection mode: frustum, orthographic of logarithmic frustum
     * @return projection declaration
     */
    fun projection(width: Float, height: Float, near: Float, far: Float, mode: ProjectionMode = frustum()): ProjectionDeclaration

    /**
     * Creates a camera declaration.
     *
     * @param position camera position
     * @param direction camera look direction
     * @param up up direction
     * @return camera declaration
     */
    fun camera(position: Vec3, direction: Vec3, up: Vec3): CameraDeclaration

    /**
     * Returns frustum projection mode.
     * @return projection mode
     */
    fun frustum(): ProjectionMode

    /**
     * Returns orthographic projection mode.
     * @return projection mode
     */
    fun ortho(): ProjectionMode

    /**
     * Returns logarithmic depth frustum projection mode. Use with larger depth ranges.
     * @return projection mode
     */
    fun log(c: Float = 1.0f): ProjectionMode

    /**
     * Creates an empty image.
     *
     * @param width image width in pixels
     * @param height image height in pixels
     * @param format image pixel formal
     * @return image
     */
    fun createImage(width: Int, height: Int, format: PixelFormat): Image

    /**
     * Creates an image from resource.
     *
     * @param imageResource resource file name
     * @return image (deferred)
     */
    fun loadImage(imageResource: String): Deferred<Image>

    /**
     * Creates an image from raw bytes in png or jpg format.
     *
     * @param bytes image file bytes
     * @return type png or jpg
     */
    fun loadImage(bytes: ByteArray, type: String): Deferred<Image>

    /**
     * Creates an empty 3D image.
     *
     * @param width image width in pixels
     * @param height image height in pixels
     * @param depth image depth in pixels
     * @param format image pixel formal
     * @return image
     */
    fun createImage3D(width: Int, height: Int, depth: Int, format: PixelFormat): Image3D

    /**
     * Returns variance shadow mapping algorithm declaration.
     * VSM algorithm provides soft shadows with good filtering, but light leaking prone.
     *
     * @param blurRadius shadow softness radius
     * @return shadow algorithm declaration
     */
    fun vsm(blurRadius: Float? = null): ShadowAlgorithmDeclaration

    /**
     * Returns hard shadow mapping algorithm declaration.
     * Fast but aliased hard shadows.
     *
     * @return shadow algorithm declaration
     */
    fun hard(): ShadowAlgorithmDeclaration

    /**
     * Returns software-based percentage close filtering shadow algorithm declaration.
     * Software PCF provides adjustable shadow softening.
     *
     * @param samples number of softening samples
     * @param blurRadius filtering kernel size
     * @param bias shadow mapping bias: smaller values may cause acne artifact, larger values cause peter-panning
     * @return shadow algorithm declaration
     */
    fun softwarePcf(samples: Int = 8, blurRadius: Float = 0.005f, bias: Float = 0.0005f): ShadowAlgorithmDeclaration

    /**
     * Returns GPU-based percentage close filtering shadow algorithm declaration.
     * GPU PCF provides faster shadow softening.
     *
     * @param bias shadow mapping bias: smaller values may cause acne artifact, larger values cause peter-panning
     * @return shadow algorithm declaration
     */
    fun hardwarePcf(bias: Float = 0.005f): ShadowAlgorithmDeclaration

    /**
     * Creates a geometry prefab for clipmap terrain.
     *
     * @param id unique declaration id
     * @param cellSize terrain cell size (at the highest resolution)
     * @param hg parameter affecting number of cells in a clipmap ring
     * @param rings number of visible rings
     * @return terrain geometry prefab
     */
    fun clipmapTerrainPrefab(id: String, cellSize: Float, hg: Int, rings: Int): Prefab

    /**
     * Creates mesh instancing declaration.
     *
     * @param id unique declaration id
     * @param count maximum number of instances
     * @param dynamic set to true to update instances each frame
     * @param block instances declaration block
     * @return mesh instancing declaration
     */
    fun instancing(id: String, count: Int, dynamic: Boolean, block: InstancedRenderablesContext.() -> Unit): InstancingDeclaration

    /**
     * Creates GLTF object instancing declaration.
     *
     * @param id unique declaration id
     * @param count maximum number of instances
     * @param dynamic set to true to update instances each frame
     * @param block instances declaration block
     * @return GLTF object instancing declaration
     */
    fun gltfInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedGltfContext.() -> Unit): GltfInstancingDeclaration

    /**
     * Creates billboard object instancing declaration.
     *
     * @param id unique declaration id
     * @param count maximum number of instances
     * @param dynamic set to true to update instances each frame
     * @param block instances declaration block
     * @return billboard instancing declaration
     */
    fun billboardInstancing(id: String, count: Int, dynamic: Boolean, block: InstancedBillboardsContext.() -> Unit): BillboardInstancingDeclaration

    /**
     * Defines the retention policy to unload an object from GPU immediately after it was not used in a rendering frame.
     *
     * @return retention policy
     */
    fun immediatelyFree(): RetentionPolicy

    /**
     * Defines the retention policy to keep objects in GPU forever.
     *
     * @return retention policy
     */
    fun keepForever(): RetentionPolicy

    /**
     * Defines the retention policy to unload unused objects from GPU when the declared retention generation is greater then the object's generation.
     *
     * @param generation retention generation
     * @return retention policy
     */
    fun untilGeneration(generation: Int): RetentionPolicy

    /**
     * Defines the retention policy to unload unused objects from GPU after the specified amount of time.
     *
     * @param seconds extra time to keep objects in GPU, in seconds
     * @return retention policy
     */
    fun time(seconds: Float): RetentionPolicy


    /**
     * Platform where the application is running.
     */
    val target: TargetPlatform

    enum class TargetPlatform {
        Desktop,
        Android,
        Web
    }
}
