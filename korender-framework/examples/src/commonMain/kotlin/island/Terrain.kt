import com.zakgof.korender.Image
import com.zakgof.korender.Prefab
import com.zakgof.korender.TerrainMaterial
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.island.loadBinary
import com.zakgof.korender.math.Vec2

fun loadRunway(bytes: ByteArray): Pair<Vec2, Vec2> = loadBinary(bytes) {
    val p1 = getVec2()
    val p2 = getVec2()
    p1 to p2
}

fun FrameContext.island(heightMap: Image, rwSeeds: Pair<Vec2, Vec2>, terrain: Prefab<TerrainMaterial>) {
    Prefab(
        terrain {
            metallicFactor = 0.0f

            plugin("normal", "!shader/plugin/normal.terrain.frag")
            plugin("terrain", "island/terrain/shader/height.glsl")
            plugin("albedo", "island/terrain/shader/albedo.glsl")
            texture("heightTexture", texture("base-terrain", heightMap))
            texture("patchTexture", texture("island/terrain/color.png"))
            texture("sdf", texture("island/terrain/sdf.png", TextureFilter.Linear))
            texture("road", texture("infcity/road.jpg"))
            texture("grassTexture", texture("texture/grass.jpg"))
            texture("runwayTexture" ,texture("island/terrain/runway.jpg"))
            vec2("runwayP1", rwSeeds.first)
            vec2("runwayP2", rwSeeds.second)
            defs ("NO_SHADOW_CAST")
        },
        prefab = terrain
    )
}
