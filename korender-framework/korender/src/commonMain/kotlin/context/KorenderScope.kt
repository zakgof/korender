package com.zakgof.korender.context

import com.zakgof.korender.BaseMaterialContext
import com.zakgof.korender.BillboardEffect
import com.zakgof.korender.BillboardMaterial
import com.zakgof.korender.BillboardMaterialContext
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.CubeTextureImages
import com.zakgof.korender.DecalMaterial
import com.zakgof.korender.Image
import com.zakgof.korender.Image3D
import com.zakgof.korender.KeyHandler
import com.zakgof.korender.Material
import com.zakgof.korender.MaterialContext
import com.zakgof.korender.MeshAttribute
import com.zakgof.korender.MutableMesh
import com.zakgof.korender.PipeMaterial
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.PostProcessMaterialContext
import com.zakgof.korender.PostProcessingMaterial
import com.zakgof.korender.ProjectionDeclaration
import com.zakgof.korender.ProjectionMode
import com.zakgof.korender.RetentionPolicy
import com.zakgof.korender.ShadowAlgorithmDeclaration
import com.zakgof.korender.SkyMaterial
import com.zakgof.korender.TerrainMaterial
import com.zakgof.korender.TerrainMaterialContext
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.Vec3.Companion.ZERO
import kotlinx.coroutines.Deferred

interface KorenderScope : ResourceScope {

    fun Frame(block: FrameScope.() -> Unit)
    fun OnTouch(handler: TouchHandler)
    fun OnKey(handler: KeyHandler)

    /** Current camera */
    var camera: CameraDeclaration

    /** Current projection */
    var projection: ProjectionDeclaration

    /** Background clear color */
    var background: ColorRGBA

    /** Current object retention generation */
    var retentionGeneration: Int

    /** Viewport width */
    val width: Int

    /** Viewport height */
    val height: Int

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
    fun captureEnv(resolution: Int, near: Float, far: Float, position: Vec3 = ZERO, insideOut: Boolean = false, block: FrameScope.() -> Unit): Deferred<CubeTextureImages>

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
    fun captureFrame(width: Int, height: Int, camera: CameraDeclaration, projection: ProjectionDeclaration, block: FrameScope.() -> Unit): Deferred<Image>

    /**
     * Creates an empty mutable mesh object.
     *
     * @return mutable mesh
     */
    fun mutableMesh(): MutableMesh

    /**
     * Creates a material that applies custom vertex and fragment shaders
     */
    fun customMaterial(vertShaderFile: String, fragShaderFile: String, block: MaterialContext.() -> Unit): Material

    /**
     * Creates a material that applies a custom vertex shader and standard fragment shader
     */
    fun customMaterial(vertShaderFile: String, block: BaseMaterialContext.() -> Unit): Material

    fun base(block: BaseMaterialContext.() -> Unit): Material

    fun billboard(block: BillboardMaterialContext.() -> Unit): BillboardMaterial

    fun terrain(block: TerrainMaterialContext.() -> Unit): TerrainMaterial

    fun decal(block: BaseMaterialContext.() -> Unit): DecalMaterial


    /**
     * Creates a material modifier for pipe shapes
     * Used with the base material.
     * @return material modifier
     */
    fun pipe(block: BaseMaterialContext.() -> Unit = {}): PipeMaterial

    // TODO
//    /**
//     * Creates a material modifier for convex radiant mapping shapes
//     * Used with the base material.
//     * @param radiantTexture cube texture declaration representing distance from the object center to the surface at the given direction
//     * @param radiantNormalTexture cube texture declaration representing object convex shape normal at the surface point at the given direction from the object center
//     * @param radiantNormalTexture cube texture declaration representing albedo color of the surface point at the given direction to the object center
//     * @param radiantNormalTexture cube texture declaration for local normal at the surface point at the given direction to the object center
//     * @return material modifier
//     */
//    fun radiant(radiantTexture: CubeTextureDeclaration, radiantNormalTexture: CubeTextureDeclaration, colorTexture: CubeTextureDeclaration, normalTexture: CubeTextureDeclaration): MaterialModifier
//
//    /**
//     * Material modifier that outputs distance to origin when capturing environment probes
//     * @param radiantMax max distance to origin
//     * @return material modifier
//     */
//    fun radiantCapture(radiantMax: Float): MaterialModifier

    /**
     * Material modifier that outputs normal map when capturing environment probes.
     * @return material modifier
     */
    // fun normalCapture(): MaterialModifier

    /**
     * Creates one-dimensional gaussian blur material modifier in horizontal direction.
     * @param radius blur radius in pixels
     * @return material modifier
     */
    fun blurHorz(radius: Float): PostProcessingMaterial

    /**
     * Creates one-dimensional gaussian blur material modifier in vertical direction.
     * @param radius blur radius in pixels
     * @return material modifier
     */
    fun blurVert(radius: Float): PostProcessingMaterial

    /**
     * Creates material modifier to adjust scene's brighness/contract/saturation.
     * Used as a post processing effect.
     * @param brightness brightness adjustment, -1..1 (0 does not change)
     * @param contrast contrast adjustment, 0..infinity (1 does not change)
     * @param contrast saturation adjustment, 0..infinity (1 does not change)
     * @return material modifier
     */
    fun adjust(brightness: Float = 0.0f, contrast: Float = 1.0f, saturation: Float = 1.0f): PostProcessingMaterial

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
    fun water(waterColor: ColorRGB = ColorRGB(0x00182A), transparency: Float = 0.1f, waveScale: Float = 25.0f, waveMagnitude: Float = 0.3f, sky: SkyMaterial): PostProcessingMaterial

    /**
     * Creates fog as material modifier.
     * Used as a post processing effect.
     *
     * @param density fog density factor
     * @param color fog color
     * @return material modifier
     */
    fun fog(density: Float = 0.00003f, color: ColorRGB = ColorRGB(0xB8CAE9)): PostProcessingMaterial

    /**
     * Creates an FXAA material modifier.
     * Used as a post processing effect.
     * @return material modifier
     */
    fun fxaa(): PostProcessingMaterial

    fun customPostProcessingFilter(fragmentShaderFile: String, block: PostProcessMaterialContext.() -> Unit = {}): PostProcessingMaterial

    /**
     * Creates a fire effect.
     * Normally used on a billboard.
     * @param strength fire strength factor 1..5
     * @return material modifier
     */
    fun fire(strength: Float = 3.0f): BillboardEffect

    /**
     * Creates a fireball effect.
     * Normally used on a billboard.
     * @param power power fireball explosion phase 0..1
     * @return material modifier
     */
    fun fireball(power: Float = 0.5f): BillboardEffect

    /**
     * Creates a single smoke ball effect.
     * Normally used on a billboard.
     * @param density smoke density
     * @param seed seed for randomness (0..1)
     * @return material modifier
     */
    fun smoke(density: Float = 0.5f, seed: Float = 0f): BillboardEffect

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
        cloudDark: Float = 0.7f,
        block: (MaterialContext) -> Unit = {},
    ): SkyMaterial

    /**
     * Creates a night sky with stars material modifier.
     * @param colorness saturation factor for stars
     * @param density factor for amount of stars
     * @param speed star motion speed factor
     * @param size star size factor
     * @return material modifier
     */
    fun starrySky(colorness: Float = 0.8f, density: Float = 20.0f, speed: Float = 1.0f, size: Float = 15.0f, block: MaterialContext.() -> Unit = {}): SkyMaterial

    /**
     * Creates a sky material modifier from a cube texture.
     * @param cubeTexture cube texture declaration
     * @return material modifier
     */
    fun cubeSky(cubeTexture: CubeTextureDeclaration, block: MaterialContext.() -> Unit = {}): SkyMaterial

    /**
     * Creates a sky material modifier from a flat texture.
     * @param texture texture declaration
     * @return material modifier
     */
    fun textureSky(texture: TextureDeclaration, block: MaterialContext.() -> Unit = {}): SkyMaterial


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

    val POS: MeshAttribute<Vec3>
    val NORMAL: MeshAttribute<Vec3>
    val TEX: MeshAttribute<Vec2>
    val JOINTS_BYTE: MeshAttribute<ByteArray>
    val JOINTS_SHORT: MeshAttribute<ShortArray>
    val JOINTS_INT: MeshAttribute<IntArray>
    val WEIGHTS: MeshAttribute<FloatArray>
    val SCALE: MeshAttribute<Vec2>
    val COLORTEXINDEX: MeshAttribute<Byte>
    val B1: MeshAttribute<Byte>
    val B2: MeshAttribute<Byte>
    val B3: MeshAttribute<Byte>
    val MODEL0: MeshAttribute<FloatArray>
    val MODEL1: MeshAttribute<FloatArray>
    val MODEL2: MeshAttribute<FloatArray>
    val MODEL3: MeshAttribute<FloatArray>
    val INSTPOS: MeshAttribute<Vec3>
    val INSTSCALE: MeshAttribute<Vec2>
    val INSTROT: MeshAttribute<Float>
    val INSTTEX: MeshAttribute<FloatArray>
    val INSTSCREEN: MeshAttribute<FloatArray>
}



