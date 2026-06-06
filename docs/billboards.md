# Billboards

A _billboard_ or _impostor_ is a flat 2D object (usually a textured quad) that always faces the camera, no matter where the camera moves. It's a common trick used to represent complex visuals with simple geometry.

To render a billboard, add the `Billboard` function to your `Frame` context with a `billboard` material:

```kotlin
Billboard(
    material = billboard {
        colorTexture = texture("textures/sprite.png")
        position = Vec3(3f, 5f, 7f)
        scale = Vec2(2f, 2f)
        rotation = 0.3f
    },
    transparent = true
)
```

The `billboard { ... }` block configures `BillboardMaterialScope`, which extends `BaseMaterialScope` with all its properties (color, colorTexture, metallicFactor, roughnessFactor, etc.) plus the following:

| Property   | Type               | Default     | Description                                    |
|------------|--------------------|-------------|------------------------------------------------|
| position   | Vec3               | (0, 0, 0)   | World position of billboard's center           |
| scale      | Vec2               | (1, 1)      | Billboard's size (horizontal/vertical)         |
| rotation   | Float              | 0.0f        | Rotation (in radians)                          |
| effect     | BillboardEffect?   | null        | Particle effect (fire, fireball, smoke)        |

Billboard instancing is also supported for rendering many billboards efficiently:

```kotlin
Billboard(
    material = billboard { colorTexture = texture("textures/particle.png") },
    transparent = true,
    instancing = billboardInstancing("particles", 100, true) {
        repeat(100) { i ->
            Instance(pos = ..., scale = ..., rotation = ..., color = ...)
        }
    }
)
```
