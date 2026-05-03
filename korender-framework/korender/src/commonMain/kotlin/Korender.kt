package com.zakgof.korender

import androidx.compose.runtime.Composable
import com.zakgof.korender.context.KorenderScope

/**
 * Creates a 3D Korender viewport for integration into Compose Multiplatform UI.
 *
 * This is the main entry point for Korender. Call this from a @Composable function
 * to create a 3D rendering viewport. The viewport automatically integrates into the
 * Compose layout system and receives touch/keyboard events from the UI.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     Column {
 *         Text("My 3D Scene")
 *         Korender(resourceLoader = { resource -> loadBytes(resource) }) {
 *             Frame {
 *                 Renderable(
 *                     material = base(color = ColorRGBA.White),
 *                     mesh = sphere(1.0f)
 *                 )
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param resourceLoader function to load asset resources (textures, shaders, models).
 *        Must return the file bytes for the given resource path.
 * @param vSync enable vertical sync (frame rate synchronized to display refresh rate).
 *        Default is false for unlimited frame rate.
 * @param block KorenderScope DSL block containing Frame declarations and configuration.
 *        Code here runs on initialization and per-frame.
 */
@Composable
expect fun Korender(
    resourceLoader: ResourceLoader = { throw KorenderException("No application resource provided") },
    vSync: Boolean = false,
    block: KorenderScope.() -> Unit
)

/**
 * Function type for loading resources (textures, shaders, models, etc.).
 * Must suspend to allow async loading.
 *
 * @param resourcePath path to the resource (e.g., "textures/diffuse.png")
 * @return the resource data as a byte array
 * @throws KorenderException if the resource cannot be loaded
 */
typealias ResourceLoader = suspend (String) -> ByteArray

/**
 * Exception thrown by Korender rendering engine.
 * Typically indicates resource loading failures or rendering errors.
 *
 * @param message error description
 */
class KorenderException(message: String) : RuntimeException(message)

/**
 * Frame timing and performance information.
 * Available within Frame context for time-dependent animations.
 *
 * Example:
 * ```kotlin
 * Frame {
 *     val rotation = frameInfo.time * 360.0f // Rotate at constant speed
 *     Renderable(material, mesh, transform = rotation(rotation.degrees))
 * }
 * ```
 *
 * @param frame absolute frame counter (incremented each frame, starting at 0)
 * @param time elapsed time in seconds since Korender started
 * @param dt delta time since last frame in seconds (frame duration)
 * @param avgFps average frames per second over recent frames
 */
class FrameInfo(
    val frame: Long,
    val time: Float,
    val dt: Float,
    val avgFps: Float,
) {
    constructor() : this(0L, 0f, 0f, 0f)
}

/**
 * Policy determining how long rendered objects persist in the GPU.
 * Used with retentionGeneration to implement LOD and object pooling.
 */
interface RetentionPolicy

/**
 * Custom shader plugin for extending shader rendering capabilities.
 * Plugins can inject custom GLSL code into various shader stages.
 *
 * @see ShaderPluginId
 */
interface ShaderPlugin

/**
 * Identifier for standard shader pipeline stages.
 * These plugins control different aspects of the rendering pipeline.
 */
enum class ShaderPluginId {
    /** Source texture loading and sampling */
    TEXSOURCE,
    /** Texture coordinate and sampling strategy */
    TEXTURING,
    /** Albedo/base color computation */
    ALBEDO,
    /** Depth/parallax mapping */
    DEPTH,
    /** Fragment discard logic (alpha cutoff, clipping) */
    DISCARD,
    /** Emission/glow color */
    EMISSION,
    /** Metallic-roughness PBR factors */
    METALLIC_ROUGHNESS,
    /** Normal mapping */
    NORMAL,
    /** Ambient occlusion */
    OCCLUSION,
    /** Final output color */
    OUTPUT,
    /** Vertex position transformation */
    POSITION,
    /** Secondary sky reflection */
    SECSKY,
    /** Sky rendering */
    SKY,
    /** Specular-glossiness PBR factors (alternative to metallic-roughness) */
    SPECULAR_GLOSSINESS,
    /** Terrain-specific rendering */
    TERRAIN,
    /** Vertex normal in world space */
    VNORMAL,
    /** Vertex position in world space */
    VPOSITION,
    /** View projection matrix transformation */
    VPROJECTION
}

interface ShaderFlag
