package com.zakgof.korender.impl.engine

import com.zakgof.korender.KorenderException
import com.zakgof.korender.TouchHandler
import com.zakgof.korender.declaration.MeshDeclaration
import com.zakgof.korender.declaration.StandardMaterialOption
import com.zakgof.korender.declaration.UniformSupplier
import com.zakgof.korender.math.Color
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

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
        if (defaultPass.renderables.isNotEmpty() || defaultPass.guis.isNotEmpty()) {
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

    fun add(renderable: RenderableDeclaration) = renderables.add(renderable)
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
        stdOptions: Set<StandardMaterialOption>,
        plugins: Map<String, String>
    ) : this(vertFile, fragFile, defs + stdOptionsToDefs(stdOptions, plugins), plugins)
}


private fun stdOptionsToDefs(
    stdOptions: Set<StandardMaterialOption>,
    plugins: Map<String, String>
): Set<String> {
    // TODO: this is ugly
    val set = HashSet<String>()
    stdOptions.forEach {
        when (it) {
            StandardMaterialOption.Color -> set.add("COLOR")
            StandardMaterialOption.Triplanar -> set.add("TRIPLANAR")
            StandardMaterialOption.Aperiodic -> set.add("APERIODIC")
            StandardMaterialOption.NormalMap -> set.add("NORMAL_MAP")
            StandardMaterialOption.Detail -> set.add("DETAIL")
            StandardMaterialOption.NoLight -> set.add("NO_LIGHT")
            StandardMaterialOption.Pcss -> set.add("PCSS")
            StandardMaterialOption.NoShadowCast -> set.add("NO_SHADOW_CAST") // TODO: this is ugly
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