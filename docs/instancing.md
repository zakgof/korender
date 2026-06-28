# Instancing

To improve rendering performance, Korender supports instancing (batching) for renderables, billboards, and models — a technique where multiple objects are rendered in a single draw call from a shared GPU buffer.

### Instanced Renderables

To render multiple meshes in a batch, add an `instancing` parameter when declaring a `Renderable`:

```kotlin
Frame {
    Renderable(
        material = base { color = ColorRGBA.Red },
        mesh = cube(0.3f),
        instancing = instancing(id = "cubes", count = 3, dynamic = true, TRANSFORM_INSTANCING) {
            Instance(transform = translate(1f, 0f, 0f))
            Instance(transform = translate(2f, 0f, 0f))
            Instance(transform = translate(3f, 0f, 0f))
        }
    )
}
```

`instancing(id, count, dynamic, vararg parameters, block)` parameters:

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String | Unique identifier |
| `count` | Int | Maximum number of instances |
| `dynamic` | Boolean | Set to `true` if instances change each frame |
| `parameters` | InstancingParameter | Which instance attributes to enable (`TRANSFORM_INSTANCING`, `COLOR_INSTANCING`, `METALLIC_INSTANCING`, `ROUGHNESS_INSTANCING`, `COLOR_TEXTURE_INDEX_INSTANCING`) |

Each `Instance` in the block can set:

| Parameter | Type | Description | Required `InstancingParameter` |
|-----------|------|-------------|-------------------------------|
| `transform` | Transform? | Spatial transform | `TRANSFORM_INSTANCING` |
| `color` | ColorRGBA? | Base color modifier | `COLOR_INSTANCING` |
| `metallic` | Float? | Metallic factor override | `METALLIC_INSTANCING` |
| `roughness` | Float? | Roughness factor override | `ROUGHNESS_INSTANCING` |
| `colorTextureIndex` | Int? | Index into the material's texture array | `COLOR_TEXTURE_INDEX_INSTANCING` |

Only the parameters whose corresponding `InstancingParameter` is passed to `instancing()` are available in the shader — include only what you need to minimize GPU buffer size.

### Instanced Billboards

Billboards can also be instanced:

```kotlin
Billboard(
    material = billboard { colorTexture = texture("textures/splat.png") },
    transparent = true,
    instancing = billboardInstancing(id = "particles", count = 3, dynamic = false, POSITION_BILLBOARD_INSTANCING, SCALE_BILLBOARD_INSTANCING) {
        Instance(pos = Vec3(1f, 0f, 0f), scale = Vec2(2f, 2f))
        Instance(pos = Vec3(2f, 0f, 0f), scale = Vec2(3f, 2f))
        Instance(pos = Vec3(3f, 0f, 0f), scale = Vec2(2f, 3f))
    }
)
```

`billboardInstancing(id, count, dynamic, vararg parameters, block)` — `parameters` selects which attributes are enabled (`POSITION_BILLBOARD_INSTANCING`, `SCALE_BILLBOARD_INSTANCING`, `ROTATION_BILLBOARD_INSTANCING`, `COLOR_BILLBOARD_INSTANCING`, `COLOR_TEXTURE_INDEX_BILLBOARD_INSTANCING`). Each `Instance` can set `pos`, `scale`, `rotation`, `color`, and `colorTextureIndex`.

### Instanced Models

All model formats (GLTF, OBJ, KR) support instancing for rendering many copies efficiently:

```kotlin
Frame {
    Model(
        resource = "model.glb",
        instancing = modelInstancing(id = "crowd", count = 10, dynamic = true) {
            repeat(10) { i ->
                Instance(
                    transform = translate(i * 2f, 0f, 0f),
                    time = i * 0.1f,     // animation time offset
                    animation = 0         // animation index
                )
            }
        }
    )
}
```

`modelInstancing(id, count, dynamic, block)` — each `Instance` can set `transform`, `time` (animation time offset), and `animation` (animation index).
