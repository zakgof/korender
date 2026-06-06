# Terrain

Korender supports optimized rendering of large terrain surfaces by implementing an algorithm based on the idea of [Geometry Clipmaps](https://developer.nvidia.com/gpugems/gpugems2/part-i-geometric-complexity/chapter-2-terrain-rendering-using-gpu-based-geometry).
This algorithm allows to drastically reduce the number of triangles required to render a large terrain while minimizing visual details loss and LOD transitions.
Basically, Korender draws the terrain as the series of clipmap rings centered at the viewpoint. Rings represent LOD levels — the more distant the ring is, the coarser triangles it contains.
Transitions between rings are smoothed using vertex morphing, which eliminates vertex popping during viewport moving.

To render a terrain, use `HeightField` within a Frame context:

```kotlin
Frame {
    HeightField(id = "terrain", cellSize = 2.0f, hg = 10, rings = 6) {
        colorTexture = texture("terrain/diffuse.png")
        heightTexture(
            heightTexture = texture("terrain/heightmap.png"),
            heightScale = 200.0f,
            outsideHeight = 0.0f,
            terrainCenter = Vec3(0f, 300f, 0f)
        )
    }
}
```

`HeightField` parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `id` | String | — | Unique identifier |
| `cellSize` | Float | — | World space size of a terrain cell |
| `hg` | Int | — | Parameter defining ring width: the higher the value, the more geometry details are preserved |
| `rings` | Int | — | Number of clipmap rings |
| `block` | TerrainMaterialScope.() -> Unit | — | Terrain material configuration |

The visible terrain extent from the viewpoint is roughly `2 * hg * 2^rings` terrain cells.

The block configures the terrain material via `TerrainMaterialScope`, which extends `BaseMaterialScope` with all its properties (color, colorTexture, normalTexture, metallicFactor, roughnessFactor, etc.) plus the following:

| Method | Parameters | Description |
|--------|------------|-------------|
| `heightTexture` | `heightTexture: TextureDeclaration`, `heightScale: Float`, `outsideHeight: Float = 0f`, `terrainCenter: Vec3 = Vec3.ZERO` | Configures the heightmap: texture (uses red channel for elevation), world-space height scale, fallback height outside texture, and world-space center point |
