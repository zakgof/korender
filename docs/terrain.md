# Terrain

Korender supports optimized rendering of large terrain surfaces by implementing an algorithm based on the idea of [Geometry Clipmaps](https://developer.nvidia.com/gpugems/gpugems2/part-i-geometric-complexity/chapter-2-terrain-rendering-using-gpu-based-geometry).
This algorithm allows to drastically reduce the number of triangles required to render a large terrain while minimizing visual details loss and LOD transitions.
Basically, Korender draws the terrain as the series of clipmap rings centered and the viewpoint. Rings represent LOD levels - the more distant the ring is, the coarser triangles its contains.
Transitions between rings are smoothed using vertex morphing, which eliminates vertex popping during viewport moving.

To render a terrain, first define terrain *prefab* in Korender context, then render the prefab in Frame context with `terrain` material modifier:

````kotlin
Korender {
    val terrain = clipmapTerrainPrefab(id = "terrain", cellSize = 2.0f, hg = 10, rings = 6)
    ...
    Frame {
        Renderable(
            base(...),
            terrain(
                heightTexture = texture("terrain/heightmap.png"),
                heightTextureSize = 1024,
                heightScale = 200.0f,
                outsideHeight = 0.0f,
                terrainCenter = Vec3(0f, 300f, 0f)
            ),
            prefab = terrain
        )
````

Prefab parameters:

- `id` unique identifier
- `cellSize` world space size of a terrain cell
- `hg` parameter defining ring width: the higher the value, the more geometry details will be preserved while rendering
- `rings` number of clipmap rings

Material modifier parameters:

- `heightTexture` heightmap defining height value at terrain point
- `heightTextureSize` size of the heightmap in pixels
- `heightScale` multiplier to pixel value defined by `heightTexture` (assuming the pixel value is 0..1)
- `outsideHeight` height value outside the texture extents
- `terrainCenter` world space point where the terrain center is to be placed

The visible terrain extent from the viewpoint is roughly `2*hg * 2^rings` terrain cells
