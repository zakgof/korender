package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.material.StandartMaterialOption
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.mesh.MeshDeclaration
import com.zakgof.korender.uniforms.UniformSupplier

internal class SceneDeclaration {
    var shadow: ShadowDeclaration? = null
    val defaultPass = PassDeclaration()
    val passes = mutableListOf<PassDeclaration>()

    fun addPass(pass: PassDeclaration) = passes.add(pass)
    fun addShadow(shadow: ShadowDeclaration) {
        if (this.shadow != null) {
            throw KorenderException("Only one Shadow declaration is allowed")
        }
        this.shadow = shadow
    }

    fun compilePasses() {
        if (defaultPass.renderables.isNotEmpty() || defaultPass.guis.isNotEmpty() || defaultPass.gltfs.isNotEmpty()) {
            if (passes.isNotEmpty()) {
                throw KorenderException("It is not allowed to mix Passes and renderables in Frame context")
            }
            passes.add(defaultPass)
        }
    }
}

internal class PassDeclaration {
    val renderables = mutableListOf<RenderableDeclaration>()
    val guis = mutableListOf<ElementDeclaration.Container>()
    val gltfs = mutableListOf<GltfDeclaration>()

    fun add(renderable: RenderableDeclaration) = renderables.add(renderable)
    fun add(gltfDeclaration: GltfDeclaration) = gltfs.add(gltfDeclaration)
    fun addGui(gui: ElementDeclaration.Container) = guis.add(gui)
}

internal class BillboardInstance(val pos: Vec3, val scale: Vec2 = Vec2.ZERO, val phi: Float = 0f)

internal class MeshInstance(val transform: Transform)

internal data class ShaderDeclaration(
    val vertFile: String,
    val fragFile: String,
    val defs: Set<String> = setOf(),
    val plugins: Map<String, String> = mapOf()
) {
    constructor(
        vertFile: String,
        fragFile: String,
        defs: Set<String>,
        stdOptions: Set<StandartMaterialOption>,
        plugins: Map<String, String>
    ) : this(vertFile, fragFile, defs + stdOptionsToDefs(stdOptions, plugins), plugins)
}


private fun stdOptionsToDefs(
    stdOptions: Set<StandartMaterialOption>,
    plugins: Map<String, String>
): Set<String> {
    // TODO: this is ugly
    val set = HashSet<String>()
    stdOptions.forEach {
        when (it) {
            StandartMaterialOption.FixedColor -> set.add("COLOR")
            StandartMaterialOption.Triplanar -> set.add("TRIPLANAR")
            StandartMaterialOption.Aperiodic -> set.add("APERIODIC")
            StandartMaterialOption.NormalMap -> set.add("NORMAL_MAP")
            StandartMaterialOption.Detail -> set.add("DETAIL")
            StandartMaterialOption.NoLight -> set.add("NO_LIGHT")
            StandartMaterialOption.Pcss -> set.add("PCSS")
            StandartMaterialOption.NoShadowCast -> set.add("NO_SHADOW_CAST") // TODO: this is ugly
            else -> {}
        }
    }
    return set + plugins.keys.map { "PLUGIN_" + it.uppercase() }
}

internal class RenderableDeclaration(
    val mesh: MeshDeclaration,
    val shader: ShaderDeclaration,
    val uniforms: UniformSupplier,
    val transform: Transform = Transform(),
    val bucket: Bucket = Bucket.OPAQUE
)

internal class MaterialDeclaration(
    val shader: ShaderDeclaration,
    val uniforms: UniformSupplier
)

internal sealed class ElementDeclaration {

    class Filler : ElementDeclaration()
    class Text(
        val id: Any,
        val fontResource: String,
        val height: Int,
        val text: String,
        val color: Color,
        val onTouch: TouchHandler
    ) : ElementDeclaration()

    class Image(
        val imageResource: String,
        val width: Int,
        val height: Int,
        val marginTop: Int,
        val marginBottom: Int,
        val marginLeft: Int,
        val marginRight: Int,
        val onTouch: TouchHandler
    ) : ElementDeclaration() {
        val fullWidth = width + marginLeft + marginRight
        val fullHeight = height + marginTop + marginBottom
    }

    class Container(val direction: Direction) : ElementDeclaration() {

        val elements = mutableListOf<ElementDeclaration>()
        fun add(element: ElementDeclaration) = elements.add(element)
    }
}

internal data class FrameBufferDeclaration(
    val id: String,
    val width: Int,
    val height: Int,
    val withDepth: Boolean
)

internal class ShadowDeclaration {
    val cascades = mutableListOf<CascadeDeclaration>()
    fun addCascade(cascadeDeclaration: CascadeDeclaration) = cascades.add(cascadeDeclaration)
}

internal data class CascadeDeclaration(val mapSize: Int, val near: Float, var far: Float)

internal data class GltfDeclaration(val gltfResource: String)