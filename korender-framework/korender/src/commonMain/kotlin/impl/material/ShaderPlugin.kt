package com.zakgof.korender.impl.material

import com.zakgof.korender.KorenderException
import com.zakgof.korender.ShaderFlag
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
    pluginId: ShaderPluginId,
    override val value: Int,
    override val file: String,
) : AppliedPlugin {
        
    TEXSOURCE_TEXTURE(ShaderPluginId.TEXSOURCE, 1, "!shader/plugin/texturesource.texture.frag"),
    TEXSOURCE_ARRAY(ShaderPluginId.TEXSOURCE, 2, "!shader/plugin/texturesource.array.frag"),

    TEXTURING_TRIPLANAR(ShaderPluginId.TEXTURING, 1, "!shader/plugin/texturing.triplanar.frag"),
    TEXTURING_STOCHASTIC(ShaderPluginId.TEXTURING, 2, "!shader/plugin/texturing.stochastic.frag"),

    ALBEDO_ISLAND_WINDOW(ShaderPluginId.ALBEDO, 1,  "island/building/shader/island.window.albedo.frag"),
    ALBEDO_RADIANT(ShaderPluginId.ALBEDO, 2,  "!shader/plugin/albedo.radiant.frag"),
    ALBEDO_VOLUME(ShaderPluginId.ALBEDO, 3,  "ltree/albedo.volume.frag"),

    DEPTH_LOG(ShaderPluginId.DEPTH, 1,  "!shader/plugin/depth.log.frag"),
    DEPTH_PIPE(ShaderPluginId.DEPTH, 2,  "!shader/plugin/depth.pipe.frag"),
    DEPTH_RADIANT(ShaderPluginId.DEPTH, 3,  "!shader/plugin/depth.radiant.frag"),

    EMISSION_FACTOR(ShaderPluginId.EMISSION, 1,  "!shader/plugin/emission.factor.frag"),
    EMISSION_TEXTURE(ShaderPluginId.EMISSION, 2,  "!shader/plugin/emission.texture.frag"),

    METALLIC_ROUGHNESS_TEXTURE(ShaderPluginId.METALLIC_ROUGHNESS, 1, "!shader/plugin/metallic_roughness.texture.frag"),

    NORMAL_PIPE(ShaderPluginId.NORMAL, 1,  "!shader/plugin/normal.pipe.frag"),
    NORMAL_RADIANT(ShaderPluginId.NORMAL, 2,  "!shader/plugin/normal.radiant.frag"),
    NORMAL_TERRAIN(ShaderPluginId.NORMAL, 3,  "!shader/plugin/normal.terrain.frag"),
    NORMAL_TEXTURE(ShaderPluginId.NORMAL, 4,  "!shader/plugin/normal.texture.frag"),

    OCCLUSION_TEXTURE(ShaderPluginId.OCCLUSION, 1, "!shader/plugin/occlusion.texture.frag"),

    OUTPUT_NORMAL(ShaderPluginId.OUTPUT, 1,  "!shader/plugin/output.normal.frag"),
    OUTPUT_RADIANT(ShaderPluginId.OUTPUT, 2,  "!shader/plugin/output.radiant.frag"),

    POSITION_PIPE(ShaderPluginId.POSITION, 1, "!shader/plugin/position.pipe.frag"),
    POSITION_RADIANT(ShaderPluginId.POSITION, 2, "!shader/plugin/position.radiant.frag"),

    SECSKY_MOON(ShaderPluginId.SECSKY, 1,  "infcity/moon.secsky.plugin.frag"),

    SKY_CUBE(ShaderPluginId.SKY, 1,  "!shader/plugin/sky.cube.frag"),
    SKY_FASTCLOUD(ShaderPluginId.SKY, 2,  "!shader/plugin/sky.fastcloud.frag"),
    SKY_STARRY(ShaderPluginId.SKY, 3,  "!shader/plugin/sky.starry.frag"),
    SKY_TEXTURE(ShaderPluginId.SKY, 4,  "!shader/plugin/sky.texture.frag"),

    SPECULAR_GLOSSINESS_FACTOR(ShaderPluginId.SPECULAR_GLOSSINESS, 1,  "!shader/plugin/specular_glossiness.factor.frag"),
    SPECULAR_GLOSSINESS_TEXTURE(ShaderPluginId.SPECULAR_GLOSSINESS, 2,  "!shader/plugin/specular_glossiness.texture.frag"),

    TERRAIN_TEXTURE(ShaderPluginId.TERRAIN, 1,  "!shader/plugin/terrain.texture.frag"),

    VNORMAL_SKINNING(ShaderPluginId.VNORMAL, 1,  "!shader/plugin/vnormal.skinning.vert"),

    VPOSITION_SKINNING(ShaderPluginId.VPOSITION, 1,  "!shader/plugin/vposition.skinning.vert"),

    VPROJECTION_FIXED_Y_RANGE(ShaderPluginId.VPROJECTION, 1, "!shader/plugin/vprojection.fixedyrange.vert"),
    VPROJECTION_FRUSTUM(ShaderPluginId.VPROJECTION, 2, "!shader/plugin/vprojection.frustum.vert"),
    VPROJECTION_LOG(ShaderPluginId.VPROJECTION, 3, "!shader/plugin/vprojection.log.vert"),
    VPROJECTION_ORTHO(ShaderPluginId.VPROJECTION, 4, "!shader/plugin/vprojection.ortho.vert"),
    ;

    override val id = pluginId.ordinal
    override val key = pluginId.toString().lowercase()
    
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
        val key = id.toString().lowercase()
        val mapKey = id.ordinal to file
        customByIdFile[mapKey]?.let { return it }

        val usedValues = Plugins.entries.filter { it.id == id.ordinal }.map { it.value }.toMutableSet()
        usedValues += customByIdValue.keys.filter { it.first == id.ordinal }.map { it.second }
        val value = (1..15).firstOrNull { it !in usedValues }
            ?: throw KorenderException("No available plugin values for id=${id.name}")

        val plugin = CustomPlugin(id.ordinal, value, key, file)
        customByIdValue[id.ordinal to value] = plugin
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
internal enum class Defs : ShaderFlag {

    VERTEX_TRANSFORM,
    VERTEX_COLOR,
    VERTEX_METALLIC,
    VERTEX_ROUGHNESS,
    VERTEX_COLORTEXINDEX,
    VERTEX_POS,
    VERTEX_ROT,
    VERTEX_SCALE,
    VERTEX_OCCLUSION,
    TRIPLANAR,
    SHADOW_CASTER,
    SSAO,
    HBAO,
    SSR,
    BLOOM,
    UPSAMPLE,
    VSM_SHADOW,
    NO_SHADOW_CAST;

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
