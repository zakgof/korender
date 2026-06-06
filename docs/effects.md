# Effects

Korender includes a set of predefined effect shaders, accessible through special material modifiers.
Each effect may expose adjustable parameters, which can be configured using effect-specific uniforms.

### Fire

Fire effect implements animated flames. Normally, it's used on a `Billboard` with transparency enabled:

```kotlin
Billboard(
    material = billboard {
        scale = Vec2(2f, 10f)
        effect = fire(strength = 4f)
    },
    transparent = true
)
```

Fire effect parameters:

| Parameter | Type  | Default | Description                                |
|-----------|-------|---------|--------------------------------------------|
| strength  | Float | 3.0f    | Controls how rippled the flame appears, 1-5 |

### Fireball

Fireball is a spherical fire-like effect that can be used for rendering explosions. Normally, it's used on a `Billboard` with transparency enabled:

```kotlin
Billboard(
    material = billboard {
        scale = Vec2(radius * phase, radius * phase)
        effect = fireball(power = phase)
    },
    transparent = true
)
```

Fireball effect parameters:

| Parameter | Type  | Default | Description                                                                     |
|-----------|-------|---------|---------------------------------------------------------------------------------|
| power     | Float | 0.5f    | Controls the fireball expansion: 0.0 for explosion initiation, 1.0 for fully exploded. Animate 0.0 to 1.0 for explosion effect |

### Smoke

Single spherical smoke particle effect. Normally, it's used on a batched `Billboard` with transparency enabled:

```kotlin
Billboard(
    material = billboard {
        scale = Vec2(1f, 1f)
        effect = smoke(density = 0.5f, seed = 0.1f)
    },
    transparent = true
)
```

Smoke effect parameters:

| Parameter | Type  | Default | Description                                                                      |
|-----------|-------|---------|----------------------------------------------------------------------------------|
| density   | Float | 0.5f    | Smoke density, 0-1                                                               |
| seed      | Float | 0.0f    | Randomness seed — provide unique values for uniquely looking smoke particles 0-1 |

### Water

Wavy water surface with sky reflections. Water is implemented as a post-process filter:

```kotlin
Frame {
    // Render the scene
    PostProcess(water(waveScale = 25.0f, sky = fastCloudSky()))
}
```

Water parameters:

| Parameter     | Type     | Default                 | Description              |
|---------------|----------|-------------------------|--------------------------|
| waterColor    | ColorRGB | ColorRGB(0x00182A)      | Water base color         |
| transparency  | Float    | 0.1f                    | Water transparency ratio |
| waveScale     | Float    | 25.0f                   | Waves horizontal scale   |
| waveMagnitude | Float    | 0.3f                    | Waves vertical magnitude |
| sky           | SkyMaterial | (required)           | Sky material for reflections |
