package island

import com.zakgof.korender.Image
import com.zakgof.korender.TextureFilter
import com.zakgof.korender.context.FrameScope
import com.zakgof.korender.examples.island.albedoTerrainPlugin
import com.zakgof.korender.examples.island.loadBinary
import com.zakgof.korender.examples.island.normalTerrainPlugin
import com.zakgof.korender.examples.island.terrainHeightPlugin
import com.zakgof.korender.math.Vec2

fun loadRunway(bytes: ByteArray): Pair<Vec2, Vec2> = loadBinary(bytes) {
    val p1 = getVec2()
    val p2 = getVec2()
    p1 to p2
}

fun FrameScope.island(heightMap: Image, rwSeeds: Pair<Vec2, Vec2>) {
    HeightField("terrain", 32.0f, 24, 6) {
        metallicFactor = 0.0f
        plugin(normalTerrainPlugin)
        plugin(terrainHeightPlugin)
        plugin(albedoTerrainPlugin)
        texture("heightTexture", texture("base-terrain", heightMap))
        texture("patchTexture", texture("island/terrain/color.png"))
        texture("sdf", texture("island/terrain/sdf.png", TextureFilter.Linear))
        texture("road", texture("infcity/road.jpg"))
        texture("grassTexture", texture("texture/grass.jpg"))
        texture("runwayTexture", texture("island/terrain/runway.jpg"))
        vec2("runwayP1", rwSeeds.first)
        vec2("runwayP2", rwSeeds.second)
        // defs ("NO_SHADOW_CAST")
    }
}
