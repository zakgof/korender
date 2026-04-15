package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ShaderPlugin
import com.zakgof.korender.ShaderPluginId
import com.zakgof.korender.impl.engine.ShaderDeclaration

internal interface AppliedPlugin : ShaderPlugin {
    val id: Int
    val value: Int
    val key: String
    val file: String
    fun apply1(accumulator: Long): Long
    fun apply2(accumulator: Long): Long
}

internal enum class Plugins(
    override val id: Int,
    override val value: Int,
    override val key: String,
    override val file: String,
) : AppliedPlugin {
    TEXTURING_ARRAY(ShaderPluginId.TEXTURING.id, 1, "texturing", "!shader/plugin/texturing.array.frag"),
    TEXTURING_TRIPLANAR(ShaderPluginId.TEXTURING.id, 2, "texturing", "!shader/plugin/texturing.triplanar.frag"),

    ALBEDO_ISLAND_WINDOW(ShaderPluginId.ALBEDO.id, 1, "albedo", "island/building/shader/island.window.albedo.frag"),
    ALBEDO_RADIANT(ShaderPluginId.ALBEDO.id, 2, "albedo", "!shader/plugin/albedo.radiant.frag"),
    ALBEDO_VOLUME(ShaderPluginId.ALBEDO.id, 3, "albedo", "ltree/albedo.volume.frag"),

    DEPTH_LOG(ShaderPluginId.DEPTH.id, 1, "depth", "!shader/plugin/depth.log.frag"),
    DEPTH_PIPE(ShaderPluginId.DEPTH.id, 2, "depth", "!shader/plugin/depth.pipe.frag"),
    DEPTH_RADIANT(ShaderPluginId.DEPTH.id, 3, "depth", "!shader/plugin/depth.radiant.frag"),

    DISCARD_ISLAND_FOLIAGE(ShaderPluginId.DISCARD.id, 1, "discard", "island/tree/shader/island.foliage.discard.frag"),

    EMISSION_FACTOR(ShaderPluginId.EMISSION.id, 1, "emission", "!shader/plugin/emission.factor.frag"),
    EMISSION_TEXTURE(ShaderPluginId.EMISSION.id, 2, "emission", "!shader/plugin/emission.texture.frag"),
    EMISSION_WINDOW(ShaderPluginId.EMISSION.id, 3, "emission", "infcity/window.emission.plugin.frag"),

    METALLIC_ROUGHNESS_TEXTURE(ShaderPluginId.METALLIC_ROUGHNESS.id, 1, "metallic_roughness", "!shader/plugin/metallic_roughness.texture.frag"),

    NORMAL_PIPE(ShaderPluginId.NORMAL.id, 1, "normal", "!shader/plugin/normal.pipe.frag"),
    NORMAL_RADIANT(ShaderPluginId.NORMAL.id, 2, "normal", "!shader/plugin/normal.radiant.frag"),
    NORMAL_TERRAIN(ShaderPluginId.NORMAL.id, 3, "normal", "!shader/plugin/normal.terrain.frag"),
    NORMAL_TEXTURE(ShaderPluginId.NORMAL.id, 4, "normal", "!shader/plugin/normal.texture.frag"),
    NORMAL_VOLUME(ShaderPluginId.NORMAL.id, 5, "normal", "ltree/normal.volume.frag"),

    OCCLUSION_TEXTURE(ShaderPluginId.OCCLUSION.id, 1, "occlusion", "!shader/plugin/occlusion.texture.frag"),

    OUTPUT_NORMAL(ShaderPluginId.OUTPUT.id, 1, "output", "!shader/plugin/output.normal.frag"),
    OUTPUT_RADIANT(ShaderPluginId.OUTPUT.id, 2, "output", "!shader/plugin/output.radiant.frag"),

    POSITION_PIPE(ShaderPluginId.POSITION.id, 1, "position", "!shader/plugin/position.pipe.frag"),
    POSITION_RADIANT(ShaderPluginId.POSITION.id, 2, "position", "!shader/plugin/position.radiant.frag"),

    SECSKY_MOON(ShaderPluginId.SECSKY.id, 1, "secsky", "infcity/moon.secsky.plugin.frag"),

    SKY_CUBE(ShaderPluginId.SKY.id, 1, "sky", "!shader/plugin/sky.cube.frag"),
    SKY_FASTCLOUD(ShaderPluginId.SKY.id, 2, "sky", "!shader/plugin/sky.fastcloud.frag"),
    SKY_STARRY(ShaderPluginId.SKY.id, 3, "sky", "!shader/plugin/sky.starry.frag"),
    SKY_TEXTURE(ShaderPluginId.SKY.id, 4, "sky", "!shader/plugin/sky.texture.frag"),

    SPECULAR_GLOSSINESS_FACTOR(ShaderPluginId.SPECULAR_GLOSSINESS.id, 1, "specular_glossiness", "!shader/plugin/specular_glossiness.factor.frag"),
    SPECULAR_GLOSSINESS_TEXTURE(ShaderPluginId.SPECULAR_GLOSSINESS.id, 2, "specular_glossiness", "!shader/plugin/specular_glossiness.texture.frag"),

    TERRAIN_TEXTURE(ShaderPluginId.TERRAIN.id, 1, "terrain", "!shader/plugin/terrain.texture.frag"),

    VNORMAL_SKINNING(ShaderPluginId.VNORMAL.id, 1, "vnormal", "!shader/plugin/vnormal.skinning.vert"),
    VPOSITION_SKINNING(ShaderPluginId.VPOSITION.id, 1, "vposition", "!shader/plugin/vposition.skinning.vert"),

    VPROJECTION_FIXED_Y_RANGE(ShaderPluginId.VPROJECTION.id, 1, "vprojection", "!shader/plugin/vprojection.fixedyrange.vert"),
    VPROJECTION_FRUSTUM(ShaderPluginId.VPROJECTION.id, 2, "vprojection", "!shader/plugin/vprojection.frustum.vert"),
    VPROJECTION_LOG(ShaderPluginId.VPROJECTION.id, 3, "vprojection", "!shader/plugin/vprojection.log.vert"),
    VPROJECTION_ORTHO(ShaderPluginId.VPROJECTION.id, 4, "vprojection", "!shader/plugin/vprojection.ortho.vert"),
    ;

    override fun apply1(accumulator: Long): Long {
        if (value == 0 || id !in 0..15) return accumulator
        val shift = id * 4
        val mask = 0xFL shl shift
        return (accumulator and mask.inv()) or ((value and 0xF).toLong() shl shift)
    }

    override fun apply2(accumulator: Long): Long {
        if (value == 0 || id !in 16..31) return accumulator
        val shift = (id - 16) * 4
        val mask = 0xFL shl shift
        return (accumulator and mask.inv()) or ((value and 0xF).toLong() shl shift)
    }

    companion object {
        val byIdValue = entries.associateBy { it.id to it.value }
    }
}

internal class ShaderPluginRegistry {
    private data class CustomPlugin(
        override val id: Int,
        override val value: Int,
        override val key: String,
        override val file: String,
    ) : AppliedPlugin {
        override fun apply1(accumulator: Long): Long {
            if (value == 0 || id !in 0..15) return accumulator
            val shift = id * 4
            val mask = 0xFL shl shift
            return (accumulator and mask.inv()) or ((value and 0xF).toLong() shl shift)
        }

        override fun apply2(accumulator: Long): Long {
            if (value == 0 || id !in 16..31) return accumulator
            val shift = (id - 16) * 4
            val mask = 0xFL shl shift
            return (accumulator and mask.inv()) or ((value and 0xF).toLong() shl shift)
        }
    }

    private val customByIdValue = mutableMapOf<Pair<Int, Int>, CustomPlugin>()
    private val customByIdFile = mutableMapOf<Pair<Int, String>, CustomPlugin>()

    fun registerCustom(id: ShaderPluginId, file: String): ShaderPlugin {
        val key = id.key
        val mapKey = id.id to file
        customByIdFile[mapKey]?.let { return it }

        val usedValues = Plugins.entries.filter { it.id == id.id }.map { it.value }.toMutableSet()
        usedValues += customByIdValue.keys.filter { it.first == id.id }.map { it.second }
        val value = (1..15).firstOrNull { it !in usedValues }
            ?: throw KorenderException("No available plugin values for id=${id.name}")

        val plugin = CustomPlugin(id.id, value, key, file)
        customByIdValue[id.id to value] = plugin
        customByIdFile[mapKey] = plugin
        return plugin
    }

    fun decode(declaration: ShaderDeclaration, shaderEnv: String): Pair<Set<String>, Map<String, String>> {

        val defs = mutableSetOf<String>()
        Defs.entries.forEach { def ->
            if ((declaration.defs and def.bit) != 0L) {
                defs += def.toString()
            }
        }
        defs += shaderEnv

        val plugins = mutableMapOf<String, String>()
        fun addPlugin(id: Int, value: Int) {
            if (value == 0) return
            val plugin = customByIdValue[id to value] ?: Plugins.byIdValue[id to value]
                ?: throw KorenderException("Unknown shader plugin id=$id value=$value")
            plugins[plugin.key] = plugin.file
            defs += "PLUGIN_${plugin.key.uppercase()}"
        }
        for (id in 0..15) {
            val shift = id * 4
            val value = ((declaration.plugins1 ushr shift) and 0xF).toInt()
            addPlugin(id, value)
        }
        for (id in 16..31) {
            val shift = (id - 16) * 4
            val value = ((declaration.plugins2 ushr shift) and 0xF).toInt()
            addPlugin(id, value)
        }

        return defs to plugins
    }

}

// TODO just use toString
internal enum class Defs(val defName: String) {
    BASE_COLOR_MAP( "BASE_COLOR_MAP"),
    TEXTURE_ARRAY("TEXTURE_ARRAY"),
    BLOOM("BLOOM"),
    VERTEX_TRANSFORM("VERTEX_TRANSFORM"),
    VERTEX_COLOR("VERTEX_COLOR"),
    VERTEX_METALLIC("VERTEX_METALLIC"),
    VERTEX_ROUGHNESS("VERTEX_ROUGHNESS"),
    VERTEX_COLORTEXINDEX("VERTEX_COLORTEXINDEX"),
    VERTEX_POS("VERTEX_POS"),
    VERTEX_ROT("VERTEX_ROT"),
    VERTEX_SCALE("VERTEX_SCALE"),
    SHADOW_CASTER("SHADOW_CASTER"),
    SSR("SSR"),
    UPSAMPLE("UPSAMPLE"),
    VERTEX_OCCLUSION( "VERTEX_OCCLUSION"),
    VSM_SHADOW("VSM_SHADOW"),
    NO_SHADOW_CAST("NO_SHADOW_CAST");

    val bit = 1L shl ordinal
}

internal infix fun Long.pluginOverride(code: Long) : Long {
    var result = this
    for (i in 0 until 16) {
        val shift = i * 4
        val mask = 0xFL shl shift
        val codeBits = code and mask
        if (codeBits != 0L) {
            result = (result and mask.inv()) or codeBits
        }
    }
    return result
}

internal fun Long.pluginOverride1IfNotNull(obj: Any?, plugin: Plugins) : Long =
    if (obj == null) this else plugin.apply1(this)

internal fun Long.pluginOverride2IfNotNull(obj: Any?, plugin: Plugins) : Long =
    if (obj == null) this else plugin.apply2(this)

internal fun Long.pluginOverride1(condition: Boolean, plugin: Plugins) =
    if (condition) plugin.apply1(this) else this

internal fun Long.pluginOverride2(condition: Boolean, plugin: Plugins) =
    if (condition) plugin.apply2(this) else this

internal fun Long.pluginOverride1(plugin: Plugins) =  plugin.apply1(this)

internal fun Long.pluginOverride2(plugin: Plugins) =  plugin.apply2(this)

internal fun Long.combineDefsIfNotNull(obj: Any?, defs: Defs) : Long {
    return if (obj == null) this else this or defs.bit
}

internal fun Long.combineDefsIfCondition(condition: Boolean, defs: Defs) : Long {
    return if (condition) this or defs.bit else this
}
