# Deferred rendering pipeline ![experimental](https://img.shields.io/badge/experimental-BA4040)

Korender supports deferred rendering pipeline as an experimental feature.

Deferred rendering is a technique where geometry and material data are rendered first into multiple textures called the G-buffer.
Lighting is then calculated in a separate pass using this stored data, allowing efficient handling of many dynamic lights.
This approach enhances performance in complex scenes and enables various screen-space effects.

To switch to deferred rendering, add `DeferredShading` to the `Frame` context:

```kotlin
Frame {
    DeferredShading()
}
```

## Shading material modifiers

Additional material modifiers applied during the shading pass:

```kotlin
Frame {
    val cubeMap = ...
    DeferredShading {
        Shading {
            env = cubeSky(cubeMap)
        }
    }
}
```

`Shading(block: ShadingMaterialScope.() -> Unit)` — the block configures shading material with `var env: SkyMaterial?` for environment mapping.

## Screen-space reflections

```kotlin
Frame {
    DeferredShading {
        Ssr(downsample = 2, envTexture = cubeMap)
    }
}
```

`Ssr(downsample, maxReflectionDistance, linearSteps, binarySteps, lastStepRatio, envTexture)` — traces reflected rays in screen space.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `downsample` | Int | 2 | Downsample factor for SSR framebuffer (1, 2, or 4) |
| `maxReflectionDistance` | Float | 10f | Maximum distance a reflected ray is traced |
| `linearSteps` | Int | 64 | Number of forward raytracing steps |
| `binarySteps` | Int | 5 | Number of binary search steps after forward tracing |
| `lastStepRatio` | Float | 4f | Factor to multiply forward step at max distance |
| `envTexture` | CubeTextureDeclaration? | null | Environment map fallback for off-screen reflections |

## Bloom

```kotlin
Frame {
    DeferredShading {
        Bloom(amount = 3.0f)
    }
}
```

`Bloom(threshold, amount, radius, downsample)` — standard bloom.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `threshold` | Float | 0.9f | Luminance threshold for glowing pixels |
| `amount` | Float | 3.0f | Bloom intensity |
| `radius` | Float | 16f | Bloom radius |
| `downsample` | Int | 2 | Downsample factor |

`BloomWide(threshold, amount, downsample, mips, offset, highResolutionRatio)` — Kawase blur bloom variant for wider glow.

## Screen-space ambient occlusion

```kotlin
Frame {
    DeferredShading {
        Ssao(blurRadius = 10f)
    }
}
```

`Ssao(downsample, sampleCount, radius, bias, intensity, blurRadius)` — standard SSAO.

`Hbao(downsample, sampleCount, radius, bias, intensity, blurRadius)` — horizon-based ambient occlusion (HBAO).

## Decals

```kotlin
Frame {
    DeferredShading {
        Decal(
            material = decal {
                colorTexture = texture("textures/decal.png")
                metallicFactor = 0.2f
            },
            position = pos,
            look = look,
            up = up,
            size = 1.6f
        )
    }
}
```

`Decal(material: DecalMaterial, position: Vec3, look: Vec3, up: Vec3, size: Float)` — projects a decal onto scene geometry.
