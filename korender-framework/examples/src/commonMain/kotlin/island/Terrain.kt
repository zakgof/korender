import com.zakgof.korender.Image
import com.zakgof.korender.Prefab
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.examples.island.loadBinary
import com.zakgof.korender.math.Vec2

fun loadRunway(bytes: ByteArray): Pair<Vec2, Vec2> = loadBinary(bytes) {
    val p1 = getVec2()
    val p2 = getVec2()
    p1 to p2
}

fun FrameContext.island(heightMap: Image, rwSeeds: Pair<Vec2, Vec2>, terrain: Prefab) {
    Renderable(
        base(metallicFactor = 0.0f),
        plugin("normal", "!shader/plugin/normal.terrain.frag"),
        plugin("terrain", "island/terrain/shader/height.glsl"),
        plugin("albedo", "island/terrain/shader/albedo.glsl"),
        uniforms(
            "heightTexture" to texture("base-terrain", heightMap),
            "patchTexture" to texture("island/terrain/color.png"),
            "sdf" to texture("island/terrain/sdf.png", TextureFilter.Linear),
            "road" to texture("infcity/road.jpg"),
            "grassTexture" to texture("texture/grass.jpg"),
            "runwayTexture" to texture("island/terrain/runway.jpg"),
            "runwayP1" to rwSeeds.first,
            "runwayP2" to rwSeeds.second
        ),
        defs("NO_SHADOW_CAST"),
        prefab = terrain
    )
}